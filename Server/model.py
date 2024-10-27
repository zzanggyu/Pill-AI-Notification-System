"""
알약 인식 서버 (Model Server)
- YOLO를 이용한 알약 검출
- OCR을 이용한 텍스트 인식
- VGG16을 이용한 모양 분류
- 색상 그룹 분류
작성자: [김현규]
마지막 수정: 2024-10-27
"""

###################
# Library Imports #
###################

# 기본 라이브러리
import os
import time
import re
import base64
import traceback
from functools import wraps

# 웹 서버 관련
from flask import Flask, request, jsonify
from flask_cors import CORS

# 이미지 처리
import cv2
import numpy as np
from PIL import Image

# 딥러닝 관련
import torch
import torch.nn as nn
import torchvision.transforms as transforms
from torchvision import models
from ultralytics import YOLO
import easyocr

# 데이터 분석
from sklearn.mixture import GaussianMixture

# 로깅
import logging

#################
# Configuration #
#################

# 서버 설정
PUBLIC_IP = "121.132.196.27"
MODEL_SERVER_PORT = 5000

# 모델 경로 설정
MODEL_BASE_PATH = 'G:/내 드라이브/캡스톤 디자인 최종본'
YOLO_MODEL_PATH = f'{MODEL_BASE_PATH}/Yolov8n/weights/best.pt'
VGG_MODEL_PATH = f'{MODEL_BASE_PATH}/vgg/pill_shape_classifier_3class_best.pth'
OCR_MODEL_PATH = f'{MODEL_BASE_PATH}/Easy'

# 로깅 설정
class CustomFormatter(logging.Formatter):
    """커스텀 로그 포맷터"""
    
    # ANSI 색상 코드
    grey = "\x1b[38;21m"
    blue = "\x1b[38;5;39m"
    yellow = "\x1b[38;5;226m"
    red = "\x1b[38;5;196m"
    reset = "\x1b[0m"

    def __init__(self):
        super().__init__()
        self.fmt = "%(asctime)s [%(levelname)s]: %(message)s"
        self.datefmt = "%H:%M:%S"

    def format(self, record):
        # 로그 레벨별 색상 지정
        color = self.blue  # 기본 색상
        
        if record.levelno == logging.WARNING:
            color = self.yellow
        elif record.levelno == logging.ERROR:
            color = self.red
            
        # 메시지에 색상 적용
        formatter = logging.Formatter(
            f"{color}{self.fmt}{self.reset}",
            self.datefmt
        )
        
        return formatter.format(record)

def setup_logging():
    """로깅 설정을 초기화"""
    logger = logging.getLogger(__name__)
    if logger.hasHandlers():
        logger.handlers.clear()
    
    # 로그 레벨 설정    
    logger.setLevel(logging.INFO)
    
    # 콘솔 핸들러
    console_handler = logging.StreamHandler()
    console_handler.setFormatter(CustomFormatter())
    logger.addHandler(console_handler)
    
    # 파일 핸들러
    file_handler = logging.FileHandler('pill_server.log')
    file_handler.setFormatter(
        logging.Formatter(
            '%(asctime)s - %(levelname)s - %(message)s',
            '%Y-%m-%d %H:%M:%S'
        )
    )
    logger.addHandler(file_handler)
    
    # 로거 전파 방지
    logger.propagate = False
    
    return logger

# 로거 초기화
logger = setup_logging()

####################
# Flask 앱 초기화 #
####################

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})

def log_request_response(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        start_time = time.time()
        
        # 요청 로깅
        logger.info("\n" + "="*50)
        logger.info("API 요청")
        logger.info("-"*30)
        
        if request.method == 'POST':
            if 'image' in request.json:
                image_size = len(request.json['image'])
                logger.info(f"- 이미지 크기: {image_size:,} bytes")
        
        # 함수 실행
        response = func(*args, **kwargs)
        
        # 응답 로깅
        if isinstance(response, tuple):
            response_data, status_code = response
        else:
            response_data, status_code = response, 200
            
        processing_time = time.time() - start_time
        
        logger.info("\nAPI 응답")
        logger.info("-"*30)
        logger.info(f"- 상태 코드: {status_code}")
        logger.info(f"- 처리 시간: {processing_time:.2f}초")
        
        if isinstance(response_data, dict):
            if 'error' in response_data:
                logger.error(f"• 오류: {response_data['error']}")
                
        logger.info("="*50 + "\n")
        return response
        
    return wrapper

####################
# Shape Classifier #
####################

class ShapeClassifier:
    """
    알약 모양 분류기
    - VGG16 기반 분류기
    - 3가지 클래스: 원형, 장방형, 타원형
    - ImageNet 가중치로 초기화된 모델 사용
    """
    
    def __init__(self, vgg_model_path):
        """
        모델 초기화
        Args:
            vgg_model_path (str): VGG 모델 가중치 파일 경로
        """
        # GPU 사용 가능 여부 확인
        self.device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
        logger.info(f"Shape Classifier를 {self.device} 장치에서 초기화합니다.")

        # 클래스 정의 (먼저 정의)
        self.shape_classes = ["원형", "장방형", "타원형"]

        # 모델 생성 및 가중치 로드 (shape_classes 정의 후에 호출)
        self.model = self.create_vgg_model()
        checkpoint = torch.load(vgg_model_path, map_location=self.device)
        self.model.load_state_dict(checkpoint['model_state_dict'])
        self.model.eval()
        self.model = self.model.to(self.device)

        # 이미지 전처리 파이프라인 정의
        self.transform = transforms.Compose([
            transforms.Resize((224, 224)),  # VGG16 입력 크기
            transforms.ToTensor(),
            # ImageNet 평균/표준편차로 정규화
            transforms.Normalize(
                mean=[0.485, 0.456, 0.406],
                std=[0.229, 0.224, 0.225]
            )
        ])
    
        logger.info("Shape Classifier 초기화 완료")

        

        
    def create_vgg_model(self):
        """
        VGG16 모델 생성 및 수정
        Returns:
            nn.Module: 수정된 VGG16 모델
        """
        # ImageNet 가중치로 초기화된 VGG16 모델 로드
        model = models.vgg16(weights='IMAGENET1K_V1')
        
        # 마지막 레이어를 3개 클래스로 수정
        model.classifier[6] = nn.Linear(4096, len(self.shape_classes))
        
        return model
    
    def predict_shape(self, image):
        """
        이미지에서 알약 모양 예측
        Args:
            image (numpy.ndarray or PIL.Image): 입력 이미지
            
        Returns:
            dict: 예측 결과와 각 클래스별 확률
        """
        try:
            # 이미지 형식 변환
            if isinstance(image, np.ndarray):
                image = Image.fromarray(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
            elif not isinstance(image, Image.Image):
                raise ValueError("지원하지 않는 이미지 형식입니다.")
            
            # 전처리 및 예측
            image_tensor = self.transform(image).unsqueeze(0).to(self.device)
            
            with torch.no_grad():
                outputs = self.model(image_tensor)
                _, predicted = torch.max(outputs, 1)
                probabilities = torch.nn.functional.softmax(outputs, dim=1)[0]
            
            # 결과 딕셔너리 생성
            result = {
                'predicted_class': self.shape_classes[predicted.item()],
                'probabilities': {
                    self.shape_classes[i]: prob.item()
                    for i, prob in enumerate(probabilities)
                }
            }
            
            # 로깅
            logger.info("\n모양 분류 결과:")
            logger.info(f"- 예측된 모양: {result['predicted_class']}")  # • -> -
            logger.info("- 클래스별 확률:")  # • -> -
            for shape, prob in result['probabilities'].items():
                logger.info(f"  - {shape}: {prob:.2%}")

            return result
            
        except Exception as e:
            logger.error(f"모양 분류 중 오류 발생: {str(e)}")
            return None
#########################
# Pill Recognition Model #
#########################

class PillRecognitionModel:
    """
    알약 인식을 위한 통합 모델 클래스
    - YOLO: 알약 검출
    - EasyOCR: 텍스트 인식
    - VGG16: 모양 분류
    - GMM: 색상 군집화
    """
    
    def __init__(self):
        """모델 초기화 및 설정"""
        logger.info("\n알약 인식 모델 초기화를 시작합니다...")
        
        # YOLO 모델 로드
        self.yolo_model = self.load_yolo_model()
        
        # OCR 초기화
        self.ocr_reader = easyocr.Reader(
            ['en'],  # 영어 텍스트만 인식
            model_storage_directory=OCR_MODEL_PATH,
            recog_network='english_g2'  # 정확도 높은 버전 사용
        )
        
        # 모양 분류기 초기화
        self.shape_classifier = ShapeClassifier(VGG_MODEL_PATH)
        
        # 색상 그룹 정의
        self.color_groups = {
            '하양': [
                ('하양', (210, 210, 210)), 
                ('하양', (220, 220, 220)),
                ('하양', (144, 144, 149)),
                ('하양', (240, 240, 240))
            ],
            '검정': [
                ('검정', (0, 0, 0)), 
                ('검정', (20, 20, 20))
            ],
            '회색': [
                ('회색', (80, 80, 80))
            ],
            '노랑/주황/분홍/빨강/갈색': [
                ('노랑', (255, 255, 0)),
                ('노랑', (255, 255, 100)),
                ('노랑', (178, 178, 170)),
                ('주황', (255, 165, 0)),
                ('주황', (255, 140, 0)),
                ('분홍', (255, 192, 203)),
                ('분홍', (255, 182, 193)),
                ('빨강', (255, 0, 0)),
                ('빨강', (220, 20, 60)),
                ('갈색', (139, 69, 19))
            ],
            '연두/초록/청록': [
                ('연두', (154, 205, 50)),
                ('연두', (124, 252, 0)),
                ('초록', (34, 139, 34)),
                ('초록', (60, 150, 60)),
                ('청록', (0, 255, 255))
            ],
            '파랑/남색': [
                ('파랑', (0, 0, 255)),
                ('파랑', (30, 144, 255)),
                ('파랑', (201, 227, 236)),
                ('남색', (0, 0, 128))
            ],
            '자주/보라': [
                ('자주', (255, 0, 255)),
                ('자주', (218, 112, 214)),
                ('보라', (128, 0, 128))
            ]
        }
        
        logger.info("알약 인식 모델 초기화 완료")

    def load_yolo_model(self):
        """YOLO 모델 로드"""
        try:
            if not os.path.exists(YOLO_MODEL_PATH):
                raise FileNotFoundError(f"YOLO 모델을 찾을 수 없습니다: {YOLO_MODEL_PATH}")
            
            model = YOLO(YOLO_MODEL_PATH)
            device = 'cuda' if torch.cuda.is_available() else 'cpu'
            model.to(device)
            
            logger.info(f"YOLO 모델을 {device} 장치에 로드했습니다.")
            return model
            
        except Exception as e:
            logger.error(f"YOLO 모델 로드 중 오류 발생: {str(e)}")
            raise

    def log_pill_features(self, text, color_info, shape_info):
        """
        알약의 추출된 특성을 로깅

        Args:
            text (list): 인식된 텍스트 리스트
            color_info (tuple): (RGB값, (색상그룹, 구체색상)) 형태의 튜플
            shape_info (dict): 모양 분류 결과 딕셔너리
        """
        logger.info("\n" + "="*50)
        logger.info("알약 특성 분석 결과")
        logger.info("-"*30)

        # 텍스트 정보
        if text:
            logger.info("[텍스트]")
            logger.info(f"- 인식 결과: {text}")

        # 색상 정보
        logger.info("[색상]")
        if isinstance(color_info, tuple):
            rgb, (group, specific) = color_info
            logger.info(f"- RGB 값: {rgb}")
            logger.info(f"- 색상 그룹: {group}")
            logger.info(f"- 구체적 색상: {specific}")

        # 모양 정보
        logger.info("[모양]")
        if shape_info and isinstance(shape_info, dict):
            predicted = shape_info.get('predicted_class')
            probs = shape_info.get('probabilities', {})
            logger.info(f"- 예측 결과: {predicted}")
            logger.info("- 예측 확률:")
            for shape, prob in probs.items():
                logger.info(f"  - {shape}: {prob:.2%}")

        logger.info("="*50)

    def preprocess_image(self, image):
        """
        OCR을 위한 이미지 전처리

        Args:
            image (numpy.ndarray): BGR 형식의 입력 이미지

        Returns:
            numpy.ndarray: 전처리된 그레이스케일 이미지
        """
        # BGR에서 그레이스케일로 변환
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

        # CLAHE(Contrast Limited Adaptive Histogram Equalization) 적용
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
        enhanced = clahe.apply(gray)

        # 노이즈 제거
        denoised = cv2.GaussianBlur(enhanced, (3,3), 0)

        # 언샤프 마스킹으로 선명도 향상
        gaussian_3 = cv2.GaussianBlur(denoised, (3,3), 2.0)
        unsharp_image = cv2.addWeighted(denoised, 1.5, gaussian_3, -0.5, 0)

        return unsharp_image

    def extract_text(self, image):
        """
        OCR로 텍스트 추출

        Args:
            image (numpy.ndarray): 전처리된 이미지

        Returns:
            list: 추출된 텍스트 리스트
        """
        try:
            # OCR 수행
            ocr_result = self.ocr_reader.readtext(
                image,
                detail=1,
                paragraph=False,
                min_size=20,          # 최소 텍스트 크기
                contrast_ths=0.15,    # 대비 임계값
                adjust_contrast=0.4,  # 대비 조정
                text_threshold=0.6,   # 텍스트 감지 임계값
                low_text=0.4,        # 낮은 텍스트 감도
                link_threshold=0.4,   # 텍스트 연결 임계값
                mag_ratio=1.2,       # 확대 비율
                # 알파벳과 숫자만 허용
                allowlist='ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
            )

            # 결과 필터링 및 정제
            filtered_result = []
            for (bbox, text, prob) in ocr_result:
                # 특수문자 제거
                cleaned_text = re.sub(r'[^A-Z0-9]', '', text)
                if cleaned_text:  # 빈 텍스트 제외
                    filtered_result.append((cleaned_text, prob))

            # 로깅
            texts = [text for text, _ in filtered_result]
            logger.info(f"추출된 텍스트: {texts}")

            return texts

        except Exception as e:
            logger.error(f"텍스트 추출 중 오류 발생: {str(e)}")
            return []

    def extract_pill_color(self, image):
        """
        알약의 주요 색상 추출

        Args:
            image (numpy.ndarray): BGR 형식의 입력 이미지

        Returns:
            tuple: (RGB 리스트, (색상 그룹, 구체적 색상))
        """
        try:
            # BGR에서 HSV로 변환
            hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
            pixels = hsv_image.reshape(-1, 3)

            # Gaussian Mixture Model로 주요 색상 군집화
            n_components = min(5, pixels.shape[0])
            gmm = GaussianMixture(
                n_components=n_components,
                random_state=42
            )
            gmm.fit(pixels)

            # 가장 많은 군집의 평균 색상 선택
            labels = gmm.predict(pixels)
            colors = gmm.means_.astype(int)
            counts = np.bincount(labels)

            # HSV to RGB 변환
            dominant_color_hsv = colors[np.argmax(counts)]
            dominant_color_rgb = cv2.cvtColor(
                np.uint8([[dominant_color_hsv]]),
                cv2.COLOR_HSV2RGB
            )[0][0]

            # 가장 가까운 색상 그룹 찾기
            color_name = self.get_color_name(dominant_color_rgb)

            # 로깅
            logger.info(f"추출된 색상: RGB{dominant_color_rgb.tolist()}")
            logger.info(f"색상 분류: {color_name}")

            return dominant_color_rgb.tolist(), color_name

        except Exception as e:
            logger.error(f"색상 추출 중 오류 발생: {str(e)}")
            return [0, 0, 0], ("알 수 없음", "알 수 없음")

    def get_color_name(self, rgb_color):
        """
        RGB 값에 가장 가까운 색상 이름 반환

        Args:
            rgb_color (list): RGB 색상값 리스트 [R, G, B]

        Returns:
            tuple: (색상 그룹명, 구체적 색상명)
        """
        if rgb_color is None:
            return "알 수 없음", "알 수 없음"

        min_distance = float('inf')
        closest_group = '알 수 없음'
        specific_color = '알 수 없음'

        # 모든 색상 그룹에 대해 유클리드 거리 계산
        for group_name, colors in self.color_groups.items():
            for color_name, color in colors:
                distance = sum((a - b) ** 2 for a, b in zip(rgb_color, color)) ** 0.5
                if distance < min_distance:
                    min_distance = distance
                    closest_group = group_name
                    specific_color = color_name

        return closest_group, specific_color

    def process_image(self, image):
        """
        이미지 처리 메인 함수
        알약 검출, 특성 추출 및 분석을 수행합니다.

        Args:
            image (numpy.ndarray): BGR 형식의 입력 이미지

        Returns:
            list: 처리된 알약 정보 리스트
        """
        logger.info("\n" + "="*50)
        logger.info("[이미지 분석 시작]")
        logger.info(f"- 입력 이미지 크기: {image.shape}")

        try:
            #################
            # 1. 모양 분류 #
            #################
            logger.info("\n[1단계: 모양 분류]")
            try:
                shape_result = self.shape_classifier.predict_shape(image)
                logger.info("[완료] 모양 분류 완료")
            except Exception as e:
                logger.error(f"[오류] 모양 분류 실패: {str(e)}")
                shape_result = None

            ##################
            # 2. 알약 검출  #
            ##################
            logger.info("\n[2단계: 알약 검출]")
            results = self.yolo_model(image)
            processed_results = []

            total_detections = len(results[0].boxes)
            logger.info(f"- 검출된 알약 수: {total_detections}")

            # 각 검출된 알약에 대해 처리
            for r in results:
                boxes = r.boxes
                for box in boxes:
                    x1, y1, x2, y2 = box.xyxy[0].tolist()
                    conf = box.conf[0].item()
                    cls = int(box.cls[0].item())

                    logger.info("\n[알약 분석]")
                    logger.info(f"- 신뢰도: {conf:.2%}")
                    logger.info(f"- 위치: ({x1:.1f}, {y1:.1f}, {x2:.1f}, {y2:.1f})")

                    # 검출 영역 유효성 검사
                    if x2 <= x1 or y2 <= y1:
                        logger.warning("[주의] 잘못된 경계 상자 무시")
                        continue

                    # 알약 이미지 추출
                    pill_image = image[int(y1):int(y2), int(x1):int(x2)]
                    if pill_image.size == 0:
                        logger.warning("[주의] 빈 이미지 무시")
                        continue

                    try:
                        #####################
                        # 3. 특성 추출     #
                        #####################
                        logger.info("\n[3단계: 특성 추출]")

                        # 이미지 전처리
                        preprocessed_image = self.preprocess_image(pill_image)

                        # 색상 추출
                        logger.info("- 색상 분석 중...")
                        dominant_color, color_name = self.extract_pill_color(pill_image)

                        # 텍스트 추출
                        logger.info("- 텍스트 인식 중...")
                        text = self.extract_text(preprocessed_image)

                        # 특성 로깅
                        self.log_pill_features(text, (dominant_color, color_name), shape_result)

                        # 결과 저장
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
                        logger.error(f"[오류] 알약 처리 중 오류 발생: {str(e)}")
                        continue

            ##################
            # 4. 결과 반환  #
            ##################
            logger.info("\n[4단계: 처리 완료]")
            logger.info(f"[완료] 성공적으로 처리된 알약: {len(processed_results)}개")
            logger.info("="*50 + "\n")

            return processed_results

        except Exception as e:
            logger.error(f"[오류] 이미지 처리 중 치명적 오류 발생: {str(e)}")
            logger.error(traceback.format_exc())
            return []
####################
# Flask Routes     #
####################

# 모델 인스턴스 생성
model = PillRecognitionModel()

@app.route('/process_image', methods=['POST'])
@log_request_response
def process_image():
    """
    이미지 처리 API 엔드포인트
    입력: base64로 인코딩된 이미지
    출력: 검출된 알약들의 특성 정보
    
    Returns:
        tuple: (JSON 응답, 상태 코드)
    """
    # 시작 시간 기록
    start_time = time.time()
    
    try:
        ##################
        # 입력값 검증   #
        ##################
        if 'image' not in request.json:
            logger.error("⚠ 이미지 데이터 누락")
            return jsonify({
                'error': '이미지 데이터가 필요합니다.'
            }), 400

        ###################
        # 이미지 디코딩  #
        ###################
        try:
            # base64 디코딩
            image_data = base64.b64decode(request.json['image'])
            # numpy 배열로 변환
            nparr = np.frombuffer(image_data, np.uint8)
            # OpenCV 이미지로 디코딩
            image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

            if image is None:
                raise ValueError("이미지 디코딩 실패")

        except Exception as e:
            logger.error(f"⚠ 이미지 디코딩 오류: {str(e)}")
            return jsonify({
                'error': '올바르지 않은 이미지 형식입니다.'
            }), 400

        ###################
        # 이미지 처리    #
        ###################
        results = model.process_image(image)
        
        # 처리 시간 계산
        processing_time = time.time() - start_time
        
        # 응답 생성
        response_data = {
            'results': results,
            'processing_time': processing_time
        }
        
        logger.info(f"[완료] 이미지 처리 완료: {processing_time:.2f}초 소요")
        return jsonify(response_data), 200

    except Exception as e:
        logger.error(f"⚠ 처리 중 오류 발생: {str(e)}")
        logger.error(traceback.format_exc())
        return jsonify({
            'error': str(e)
        }), 500

####################
# Main Execution  #
####################

if __name__ == '__main__':
    try:
        # 시작 배너 출력
        logger.info("\n" + "="*60)
        logger.info("[시작] 알약 인식 서버 시작")  # 🚀 이모지 제거
        logger.info("="*60)
        
        # 서버 정보 출력
        logger.info("\n[서버 정보]")
        logger.info(f"- 주소: http://{PUBLIC_IP}:{MODEL_SERVER_PORT}")
        logger.info(f"- GPU 사용: {'가능' if torch.cuda.is_available() else '불가능'}")
        if torch.cuda.is_available():
            logger.info(f"- GPU 정보: {torch.cuda.get_device_name(0)}")
        logger.info(f"- 작업 디렉토리: {os.getcwd()}")
        
        # 모델 경로 확인
        logger.info("\n[모델 경로]")
        logger.info(f"- YOLO: {YOLO_MODEL_PATH}")
        logger.info(f"- VGG: {VGG_MODEL_PATH}")  
        logger.info(f"- OCR: {OCR_MODEL_PATH}")  
        
        logger.info("\n[서버 시작 중...]")
        logger.info("="*60 + "\n")
        
        # Flask 서버 시작
        app.run(
            host='0.0.0.0',
            port=MODEL_SERVER_PORT,
            debug=False
        )
        
    except Exception as e:
        logger.error("\n[오류] 서버 실행 중 치명적 오류 발생!")  
        logger.error(f"오류 내용: {str(e)}")
        logger.error(traceback.format_exc())
        raise
