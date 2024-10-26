import cv2
import numpy as np
import easyocr
from flask import Flask, request, jsonify
import base64
from ultralytics import YOLO
from flask_cors import CORS
import os
import traceback
import time
import re
from sklearn.mixture import GaussianMixture
import logging
import torch
import torchvision.transforms as transforms
import torch.nn as nn
from torchvision import models
from PIL import Image
from functools import wraps

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(),  # 콘솔 출력
        logging.FileHandler('pill_server.log')  # 파일 출력
    ]
)
logger = logging.getLogger(__name__)

# Flask 애플리케이션 초기화
app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})

def log_request_response(func):
    """
    API 요청과 응답을 로깅하는 데코레이터
    """
    @wraps(func)
    def wrapper(*args, **kwargs):
        start_time = time.time()
        
        # 요청 로깅
        if request.method == 'POST':
            logger.info(f"\n{'='*50}\nAPI Request to {request.path}")
            logger.info(f"Request Headers: {dict(request.headers)}")
            if 'image' in request.json:
                image_size = len(request.json['image'])
                logger.info(f"Request contains image data of size: {image_size} bytes")
        
        # 함수 실행
        response = func(*args, **kwargs)
        
        # 응답 로깅
        if isinstance(response, tuple):
            response_data, status_code = response
        else:
            response_data, status_code = response, 200
        
        processing_time = time.time() - start_time
        
        logger.info(f"\nAPI Response (Status: {status_code})")
        if isinstance(response_data, dict):
            if 'results' in response_data:
                logger.info("\nDetected Pills Information:")
                for idx, pill in enumerate(response_data['results'], 1):
                    logger.info(f"\nPill #{idx}:")
                    logger.info(f"Class: {pill.get('class')}")
                    logger.info(f"Confidence: {pill.get('confidence'):.4f}")
                    logger.info(f"Bounding Box: {pill.get('bbox')}")
                    logger.info(f"Detected Text: {pill.get('text')}")
                    logger.info(f"Color: {pill.get('color')}")
                    logger.info(f"Shape: {pill.get('shape')}")
            
            if 'processing_time' in response_data:
                logger.info(f"\nTotal Processing Time: {response_data['processing_time']:.2f} seconds")
            
            if 'error' in response_data:
                logger.error(f"Error: {response_data['error']}")
        
        logger.info(f"{'='*50}\n")
        return response
    
    return wrapper

class ShapeClassifier:
    """
    알약 모양을 분류하는 VGG16 기반 분류기
    """
    def __init__(self, vgg_model_path):
        self.device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
        logger.info(f"Shape Classifier initializing on device: {self.device}")
        
        # VGG 모델 생성 및 로드
        self.model = self.create_vgg_model()
        checkpoint = torch.load(vgg_model_path, map_location=self.device)
        self.model.load_state_dict(checkpoint['model_state_dict'])
        self.model.eval()
        self.model = self.model.to(self.device)
        
        # 이미지 전처리 파이프라인
        self.transform = transforms.Compose([
            transforms.Resize((224, 224)),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
        ])
        
        self.shape_classes = ["원형", "장방형", "타원형"]
        logger.info("Shape Classifier initialized successfully")

    def create_vgg_model(self):
        model = models.vgg16(weights='IMAGENET1K_V1')
        model.classifier[6] = nn.Linear(4096, 3)  # 3개 클래스로 출력층 수정
        return model

    def predict_shape(self, image):
        """
        이미지의 알약 모양을 예측
        """
        try:
            # 이미지 형식 변환
            if isinstance(image, np.ndarray):
                image = Image.fromarray(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
            elif not isinstance(image, Image.Image):
                raise ValueError("Unsupported image type")

            # 전처리 및 예측
            image_tensor = self.transform(image).unsqueeze(0).to(self.device)
            
            with torch.no_grad():
                outputs = self.model(image_tensor)
                _, predicted = torch.max(outputs, 1)
                probabilities = torch.nn.functional.softmax(outputs, dim=1)[0]

            # 결과 반환
            result = {
                'predicted_class': self.shape_classes[predicted.item()],
                'probabilities': {
                    self.shape_classes[i]: prob.item()
                    for i, prob in enumerate(probabilities)
                }
            }
            logger.info(f"Shape prediction: {result}")
            return result
            
        except Exception as e:
            logger.error(f"Error in shape prediction: {str(e)}")
            return None

class PillRecognitionModel:
    """
    알약 인식을 위한 통합 모델
    """
    def __init__(self):
        logger.info("Initializing PillRecognitionModel...")
        
        # YOLO 모델 로드
        self.yolo_model = self.load_yolo_model()
        
        # OCR 초기화
        self.ocr_reader = easyocr.Reader(
            ['en'],
            model_storage_directory='G:/내 드라이브/캡스톤 디자인 최종본/Easy',
            recog_network='english_g2'
        )
        
        # 모양 분류기 초기화
        self.shape_classifier = ShapeClassifier(
            'G:/내 드라이브/캡스톤 디자인 최종본/vgg/pill_shape_classifier_3class_best.pth'
        )
        
        # 색상 그룹 정의
        self.color_groups = {
            '하양': [('하양', (210, 210, 210)), ('하양', (220, 220, 220)),('하양',(144, 144, 149)),
                   ('하양', (240, 240, 240))],
            '검정': [('검정', (0, 0, 0)), ('검정', (20, 20, 20))],
            '회색': [('회색', (80, 80, 80))],
            '노랑/주황/분홍/빨강/갈색': [
                ('노랑', (255, 255, 0)), ('노랑', (255, 255, 100)),('노랑', (178, 178, 170)),
                ('주황', (255, 165, 0)), ('주황', (255, 140, 0)),
                ('분홍', (255, 192, 203)), ('분홍', (255, 182, 193)),
                ('빨강', (255, 0, 0)), ('빨강', (220, 20, 60)),
                ('갈색', (139, 69, 19))
            ],
            '연두/초록/청록': [
                ('연두', (154, 205, 50)), ('연두', (124, 252, 0)),
                ('초록', (34, 139, 34)), ('초록', (60, 150, 60)),
                ('청록', (0, 255, 255))
            ],
            '파랑/남색': [
                ('파랑', (0, 0, 255)), ('파랑', (30, 144, 255)),('파랑',(201, 227, 236)),
                ('남색', (0, 0, 128))
            ],
            '자주/보라': [
                ('자주', (255, 0, 255)), ('자주', (218, 112, 214)),
                ('보라', (128, 0, 128))
            ]
        }
        logger.info("PillRecognitionModel initialized successfully")

    def load_yolo_model(self):
        """YOLO 모델 로드"""
        model_path = os.environ.get(
            'YOLO_MODEL_PATH',
            'G:/내 드라이브/캡스톤 디자인 최종본/Yolov8n/weights/best.pt'
        )
        if not os.path.exists(model_path):
            raise FileNotFoundError(f"YOLO model file not found at {model_path}")
        
        model = YOLO(model_path)
        device = 'cuda' if torch.cuda.is_available() else 'cpu'
        model.to(device)
        logger.info(f"YOLO model loaded successfully on {device}")
        return model

    def preprocess_image(self, image):
        """OCR을 위한 이미지 전처리"""
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
        enhanced = clahe.apply(gray)
        denoised = cv2.GaussianBlur(enhanced, (3,3), 0)
        gaussian_3 = cv2.GaussianBlur(denoised, (3,3), 2.0)
        unsharp_image = cv2.addWeighted(denoised, 1.5, gaussian_3, -0.5, 0)
        return unsharp_image

    def extract_text(self, image):
        """OCR로 텍스트 추출"""
        try:
            ocr_result = self.ocr_reader.readtext(
                image,
                detail=1,
                paragraph=False,
                min_size=20,
                contrast_ths=0.15,
                adjust_contrast=0.4,
                text_threshold=0.6,
                low_text=0.4,
                link_threshold=0.4,
                mag_ratio=1.2,
                allowlist='ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
            )
            
            filtered_result = []
            for (bbox, text, prob) in ocr_result:
                cleaned_text = re.sub(r'[^A-Z0-9]', '', text)
                if cleaned_text:
                    filtered_result.append((cleaned_text, prob))
            
            logger.info(f"Extracted text: {[text for text, _ in filtered_result]}")
            return [text for text, _ in filtered_result]
            
        except Exception as e:
            logger.error(f"Error in text extraction: {str(e)}")
            return []

    def extract_pill_color(self, image):
        """알약의 주요 색상 추출"""
        try:
            hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
            pixels = hsv_image.reshape(-1, 3)
            
            n_components = min(5, pixels.shape[0])
            gmm = GaussianMixture(n_components=n_components, random_state=42)
            gmm.fit(pixels)
            
            labels = gmm.predict(pixels)
            colors = gmm.means_.astype(int)
            counts = np.bincount(labels)
            
            dominant_color_hsv = colors[np.argmax(counts)]
            dominant_color_rgb = cv2.cvtColor(
                np.uint8([[dominant_color_hsv]]),
                cv2.COLOR_HSV2RGB
            )[0][0]
            
            color_name = self.get_color_name(dominant_color_rgb)
            logger.info(f"Extracted color: RGB{dominant_color_rgb.tolist()}, Name: {color_name}")
            
            return dominant_color_rgb.tolist(), color_name
            
        except Exception as e:
            logger.error(f"Error in color extraction: {str(e)}")
            return [0, 0, 0], ("알 수 없음", "알 수 없음")

    def get_color_name(self, rgb_color):
        """RGB 값에 가장 가까운 색상 이름 반환"""
        if rgb_color is None:
            return "알 수 없음", "알 수 없음"
        
        min_distance = float('inf')
        closest_group = '알 수 없음'
        specific_color = '알 수 없음'

        for group_name, colors in self.color_groups.items():
            for color_name, color in colors:
                distance = sum((a - b) ** 2 for a, b in zip(rgb_color, color)) ** 0.5
                if distance < min_distance:
                    min_distance = distance
                    closest_group = group_name
                    specific_color = color_name

        return closest_group, specific_color

    def process_image(self, image):
        """이미지 처리 메인 함수"""
        logger.info("\nStarting image processing...")
        logger.info(f"Input image shape: {image.shape}")
        
        # 전체 이미지에 대한 모양 분류
        try:
            shape_result = self.shape_classifier.predict_shape(image)
            logger.info(f"Shape classification result: {shape_result}")
        except Exception as e:
            logger.error(f"Error in shape classification: {str(e)}")
            shape_result = None

        # YOLO 객체 검출
        logger.info("Performing YOLO object detection...")
        results = self.yolo_model(image)
        processed_results = []

        # 검출된 객체 처리
        total_detections = len(results[0].boxes)
        logger.info(f"YOLO detected {total_detections} objects")

        for r in results:
            boxes = r.boxes
            for box in boxes:
                x1, y1, x2, y2 = box.xyxy[0].tolist()
                conf = box.conf[0].item()
                cls = int(box.cls[0].item())

                logger.info(f"\nProcessing detection:")
                logger.info(f"Bounding Box: ({x1:.1f}, {y1:.1f}, {x2:.1f}, {y2:.1f})")
                logger.info(f"Confidence: {conf:.4f}")

                if x2 <= x1 or y2 <= y1:
                    logger.warning(f"Skipping invalid bounding box")
                    continue

                pill_image = image[int(y1):int(y2), int(x1):int(x2)]
                if pill_image.size == 0:
                    logger.warning(f"Skipping empty pill image")
                    continue

                try:
                    # 알약 특성 추출
                    logger.info("Extracting pill features...")
                    preprocessed_image = self.preprocess_image(pill_image)
                    dominant_color, color_name = self.extract_pill_color(pill_image)
                    text = self.extract_text(preprocessed_image)

                    # 추출된 특성 로깅
                    logger.info(f"Detected text: {text}")
                    logger.info(f"Dominant color: RGB{dominant_color}, Name: {color_name}")

                    processed_results.append({
                        'class': cls,
                        'confidence': conf,
                        'bbox': [x1, y1, x2, y2],
                        'text': text,
                        'color': {
                            'rgb': dominant_color,
                            'name': color_name[0],
                            'specific': color_name[1]
                        },
                        'shape': shape_result if shape_result else {
                            'predicted_class': '알 수 없음',
                            'probabilities': {}
                        }
                    })

                except Exception as e:
                    logger.error(f"Error processing pill: {str(e)}")
                    continue

        logger.info(f"\nProcessing completed. Found {len(processed_results)} valid pills")
        return processed_results

# 모델 인스턴스 생성
model = PillRecognitionModel()

@app.route('/process_image', methods=['POST'])
@log_request_response
def process_image():
    """
    이미지 처리 API 엔드포인트
    입력: base64로 인코딩된 이미지
    출력: 검출된 알약들의 특성 정보
    """
    # 시작 시간 기록
    start_time = time.time()

    # 입력 검증
    if 'image' not in request.json:
        logger.error("No image data provided")
        return jsonify({'error': 'No image data provided'}), 400

    try:
        # base64 이미지 디코딩
        image_data = base64.b64decode(request.json['image'])
        nparr = np.frombuffer(image_data, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if image is None:
            raise ValueError("Failed to decode image")

        # 이미지 처리
        results = model.process_image(image)
        
        # 처리 시간 계산
        processing_time = time.time() - start_time
        
        # 응답 생성
        response_data = {
            'results': results,
            'processing_time': processing_time
        }
        
        logger.info(f"Successfully processed image in {processing_time:.2f} seconds")
        return jsonify(response_data), 200

    except Exception as e:
        logger.error(f"Error occurred: {str(e)}", exc_info=True)
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    try:
        logger.info('='*50)
        logger.info('서버를 시작합니다...')
        logger.info(f'서버 주소: http://121.132.196.27:5000')
        logger.info('='*50)
        app.run(host='0.0.0.0', port=5000, debug=False)
        
    except Exception as e:
        logger.error(f"서버 실행 중 오류 발생: {str(e)}", exc_info=True)
