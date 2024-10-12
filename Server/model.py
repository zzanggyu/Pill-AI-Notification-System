# 필요한 라이브러리 임포트
import cv2  # 이미지 처리를 위한 OpenCV 라이브러리
import numpy as np  # 수치 연산을 위한 NumPy 라이브러리
import easyocr  # 텍스트 인식을 위한 EasyOCR 라이브러리
from flask import Flask, request, jsonify  # 웹 서버 구축을 위한 Flask 프레임워크
import base64  # 이미지 데이터 인코딩/디코딩을 위한 라이브러리
from ultralytics import YOLO  # YOLO 객체 탐지 모델
from flask_cors import CORS  # 크로스 오리진 리소스 공유(CORS) 지원
import os  # 운영 체제 기능 사용
import traceback  # 예외 추적을 위한 라이브러리
import time  # 시간 측정을 위한 라이브러리
import re  # 정규 표현식 사용을 위한 라이브러리
from scipy import ndimage  # 이미지 처리를 위한 SciPy 라이브러리
from sklearn.cluster import KMeans, MiniBatchKMeans  # 클러스터링 알고리즘
from sklearn.utils import parallel_backend  # 병렬 처리 지원
from joblib import parallel_backend  # 병렬 처리 지원
from sklearn.mixture import GaussianMixture  # 가우시안 혼합 모델

# 로깅 설정
logging.basicConfig(level=logging.DEBUG)  # 로깅 레벨을 DEBUG로 설정
logger = logging.getLogger(__name__)  # 로거 객체 생성

# Flask 애플리케이션 생성 및 CORS 설정
app = Flask(__name__)
CORS(app)  # 모든 라우트에 대해 CORS 허용

class PillRecognitionModel:
    def __init__(self):
        # YOLO 모델 로드
        self.yolo_model = self.load_yolo_model()
        # OCR 리더 초기화
        self.ocr_reader = easyocr.Reader(['en'], model_storage_directory='G:/내 드라이브/EASY/model', recog_network='english_g2')
        # 색상 그룹 정의
        self.color_groups = {
            '하양': [('하양', (210, 210, 210)), ('하양', (220, 220, 220)), ('하양', (240, 240, 240))],
            '검정': [('검정', (0, 0, 0)), ('검정', (20, 20, 20))],
            '회색': [('회색', (180, 180, 180)), ('회색', (128, 128, 128)), ('회색', (80, 80, 80))],
            '노랑/주황/분홍/빨강/갈색': [
                ('노랑', (255, 255, 0)), ('노랑', (255, 255, 100)), ('노랑', (230, 200, 50)), ('노랑', (235, 215, 140)),
                ('주황', (255, 165, 0)), ('주황', (255, 140, 0)), ('주황', (230, 135, 25)),
                ('분홍', (240, 128, 46)), ('분홍', (255, 192, 203)), ('분홍', (255, 182, 193)), ('분홍', (210, 180, 180)),
                ('빨강', (255, 0, 0)), ('빨강', (220, 20, 60)),
                ('갈색', (139, 69, 19))
            ],
            '연두/초록/청록': [
                ('연두', (154, 205, 50)), ('연두', (124, 252, 0)), ('연두', (210, 250, 210)), ('연두', (192, 217, 197)),
                ('초록', (128, 255, 0)), ('초록', (34, 139, 34)), ('초록', (60, 150, 60)),
                ('청록', (0, 255, 255)), ('청록', (0, 206, 209))
            ],
            '파랑/남색': [
                ('파랑', (135, 206, 235)), ('파랑', (100, 149, 237)), ('파랑', (0, 0, 255)), ('파랑', (30, 144, 255)),
                ('남색', (0, 0, 128)), ('남색', (25, 25, 112))
            ],
            '자주/보라': [
                ('자주', (255, 0, 255)), ('자주', (218, 112, 214)),
                ('보라', (128, 0, 128)), ('보라', (148, 0, 211))
            ]
        }
        # K-means 클러스터링 모델 초기화
        self.kmeans = KMeans(n_clusters=3, n_init=10, random_state=42)

    # YOLO 모델 로드
    def load_yolo_model(self):
        # YOLO 모델 파일 경로 설정 (환경 변수 또는 기본 경로 사용)
        model_path = os.environ.get('YOLO_MODEL_PATH', 'G:/내 드라이브/yolo_checkpoints/pill/weights/best.pt')
        if not os.path.exists(model_path):
            raise FileNotFoundError(f"YOLO model file not found at {model_path}")
        # YOLO 모델 로드
        model = YOLO(model_path)
        return model

    # 이미지 전처리
    def preprocess_image(self, image):
        # 그레이스케일 변환
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

        # CLAHE(Contrast Limited Adaptive Histogram Equalization) 적용
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
        enhanced = clahe.apply(gray)

        # 가우시안 블러로 노이즈 제거
        denoised = cv2.GaussianBlur(enhanced, (3,3), 0)

        # 언샤프 마스크로 엣지 강화
        gaussian_3 = cv2.GaussianBlur(denoised, (3,3), 2.0)
        unsharp_image = cv2.addWeighted(denoised, 1.5, gaussian_3, -0.5, 0)

        return unsharp_image

    # 이미지를 YOLO에서 인식한 박스좌표대로 자르고 나온 이미지를 전처리 함수에 넣어 전처리 후 결과 반환
    def process_image(self, image):
        results = self.yolo_model(image)
        processed_results = []

        for r in results:
            boxes = r.boxes
            for box in boxes:
                x1, y1, x2, y2 = box.xyxy[0].tolist()
                conf = box.conf[0].item()
                cls = int(box.cls[0].item())

                # 바운딩 박스 유효성 검사
                if x2 <= x1 or y2 <= y1:
                    print(f"Skipping invalid bounding box: {x1}, {y1}, {x2}, {y2}")
                    continue

                pill_image = image[int(y1):int(y2), int(x1):int(x2)]

                if pill_image.size == 0:
                    print(f"Skipping empty pill image for bounding box: {x1}, {y1}, {x2}, {y2}")
                    continue

                preprocessed_image = self.preprocess_image(pill_image)
                dominant_color, color_name = self.extract_pill_color(image, [x1, y1, x2, y2])

                try:
                    text = self.extract_text(preprocessed_image)
                except Exception as e:
                    print(f"Error extracting text: {str(e)}")
                    text = []

                processed_results.append({
                    'class': cls,
                    'confidence': conf,
                    'bbox': [x1, y1, x2, y2],
                    'text': text,
                    'color': {
                        'rgb': dominant_color,
                        'group': color_name[0],
                        'specific': color_name[1]
                    }
                })

        return processed_results

    # EASY OCR을 사용해 텍스트 추출 
    def extract_text(self, image):
        # OCR을 사용하여 텍스트 추출
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

        # 결과 필터링 및 정제
        filtered_result = []
        for (bbox, text, prob) in ocr_result:
            cleaned_text = re.sub(r'[^A-Z0-9]', '', text)  # 알파벳과 숫자만 남김
            if cleaned_text:
                filtered_result.append((cleaned_text, prob))

        return [text for text, _ in filtered_result]

    # 색상 추출 
    def extract_pill_color(self, image, bbox):
        try:
            x1, y1, x2, y2 = map(int, bbox)
            print(f"Original bounding box coordinates: {x1}, {y1}, {x2}, {y2}")

            height, width = image.shape[:2]
            print(f"Image dimensions: {width}x{height}")

            # 좌표 조정
            x1, y1 = max(0, x1), max(0, y1)
            x2, y2 = min(width, x2), min(height, y2)

            # 너비와 높이가 양수인지 확인
            if x2 <= x1 or y2 <= y1:
                raise ValueError("Invalid bounding box: width or height is zero or negative")

            print(f"Adjusted bounding box: {x1}, {y1}, {x2}, {y2}")

            pill_image = image[y1:y2, x1:x2]
            print(f"Extracted pill image shape: {pill_image.shape}")

            if pill_image.size == 0:
                raise ValueError("Extracted pill image is empty")

            rgb_image = cv2.cvtColor(pill_image, cv2.COLOR_BGR2RGB)
            pixels = rgb_image.reshape(-1, 3)
            print(f"Reshaped pixels shape: {pixels.shape}")
            
            # 가우시안 혼합 모델(GMM) 컴포넌트 수 결정
            n_components = min(5, pixels.shape[0])
            print(f"Number of GMM components: {n_components}")
            
            # GMM 적용
            gmm = GaussianMixture(n_components=n_components, random_state=42)
            gmm.fit(pixels)
            print("GMM fitted successfully")
            
            # 주요 색상 추출
            labels = gmm.predict(pixels)
            colors = gmm.means_.astype(int)
            counts = np.bincount(labels)
            dominant_color = colors[np.argmax(counts)]
            print(f"Dominant color: {dominant_color}")
            
            # 색상 이름 결정
            color_name = self.get_color_name(dominant_color)
            print(f"Color name: {color_name}")
            
            return dominant_color.tolist(), color_name
        
        except Exception as e:
            print(f"Error in extract_pill_color: {str(e)}")
            traceback.print_exc()
            return [0, 0, 0], ("알 수 없음", "알 수 없음")

    # 추출된 색상으로 가장 근접한 색상값 반환
    def get_color_name(self, rgb_color):
        if rgb_color is None:
            return "알 수 없음", "알 수 없음"
        
        # 가장 가까운 색상 찾기
        min_distance = float('inf')
        closest_group = '알 수 없음'
        specific_color = '알 수 없음'

        for group_name, colors in self.color_groups.items():
            for color_name, color in colors:
                # 유클리디안 거리 계산
                distance = sum((a - b) ** 2 for a, b in zip(rgb_color, color)) ** 0.5
                if distance < min_distance:
                    min_distance = distance
                    closest_group = group_name
                    specific_color = color_name

        return closest_group, specific_color

# 모델 인스턴스 생성
model = PillRecognitionModel()

# 이미지 처리 API 엔드포인트
@app.route('/process_image', methods=['POST'])
def process_image():
    start_time = time.time()

    # 이미지 데이터 확인
    if 'image' not in request.json:
        return jsonify({'error': 'No image data provided'}), 400

    try:
        # 이미지 디코딩
        image_data = base64.b64decode(request.json['image'])
        nparr = np.frombuffer(image_data, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if image is None:
            raise ValueError("Failed to decode image")

        # 이미지 처리
        results = model.process_image(image)
        return jsonify({'results': results}), 200

    except Exception as e:
        # 오류 처리 및 로깅
        error_trace = traceback.format_exc()
        print(f"Error occurred: {str(e)}\n{error_trace}")
        return jsonify({'error': str(e), 'trace': error_trace}), 500

# 메인 실행 부분
if __name__ == '__main__':
    try:
        logger.info('서버를 시작합니다...')
        print('서버가 http://localhost:5000 에서 실행 중입니다.')
        app.run(host='0.0.0.0', port=5000, debug=False)
    except Exception as e:
        logger.error(f"서버 실행 중 오류 발생: {str(e)}", exc_info=True)