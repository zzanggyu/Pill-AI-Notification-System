## 10월 20일 업데이트
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

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})

class PillRecognitionModel:
    def __init__(self):
        self.yolo_model = self.load_yolo_model()
        self.ocr_reader = easyocr.Reader(['en'], model_storage_directory='G:/내 드라이브/캡스톤 디자인 최종본/Easy', recog_network='english_g2')
        self.color_groups = {
            '하양': [('하양', (210, 210, 210)), ('하양', (220, 220, 220)), ('하양', (240, 240, 240)), ('하양', (170, 170, 170))],
            '검정': [('검정', (0, 0, 0)), ('검정', (20, 20, 20))],
            '회색': [('회색', (128, 128, 128)), ('회색', (80, 80, 80))],
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

    def load_yolo_model(self):
        model_path = os.environ.get('YOLO_MODEL_PATH', 'G:/내 드라이브/캡스톤 디자인 최종본/Yolov8n/weights/best.pt')
        if not os.path.exists(model_path):
            raise FileNotFoundError(f"YOLO model file not found at {model_path}")
        model = YOLO(model_path)
        device = 'cuda' if torch.cuda.is_available() else 'cpu'
        model.to(device)
        return model

    def preprocess_image(self, image):
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
        enhanced = clahe.apply(gray)
        denoised = cv2.GaussianBlur(enhanced, (3,3), 0)
        gaussian_3 = cv2.GaussianBlur(denoised, (3,3), 2.0)
        unsharp_image = cv2.addWeighted(denoised, 1.5, gaussian_3, -0.5, 0)
        return unsharp_image

    def process_image(self, image):
        results = self.yolo_model(image)
        processed_results = []

        for r in results:
            boxes = r.boxes
            for box in boxes:
                x1, y1, x2, y2 = box.xyxy[0].tolist()
                conf = box.conf[0].item()
                cls = int(box.cls[0].item())

                if x2 <= x1 or y2 <= y1:
                    logger.warning(f"Skipping invalid bounding box: {x1}, {y1}, {x2}, {y2}")
                    continue

                pill_image = image[int(y1):int(y2), int(x1):int(x2)]

                if pill_image.size == 0:
                    logger.warning(f"Skipping empty pill image for bounding box: {x1}, {y1}, {x2}, {y2}")
                    continue

                preprocessed_image = self.preprocess_image(pill_image)
                dominant_color, color_name = self.extract_pill_color(pill_image)

                try:
                    text = self.extract_text(preprocessed_image)
                except Exception as e:
                    logger.error(f"Error extracting text: {str(e)}")
                    text = []

                processed_results.append({
                    'class': cls,
                    'confidence': conf,
                    'bbox': [x1, y1, x2, y2],
                    'text': text,
                    'color': {
                        'rgb': dominant_color,
                        'name': color_name[0],
                        'specific': color_name[1]
                    }
                })

        return processed_results

    def extract_text(self, image):
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

        return [text for text, _ in filtered_result]

    def extract_pill_color(self, image):
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
            
            dominant_color_rgb = cv2.cvtColor(np.uint8([[dominant_color_hsv]]), cv2.COLOR_HSV2RGB)[0][0]
            color_name = self.get_color_name(dominant_color_rgb)
            
            logger.info(f"Dominant color (RGB): {dominant_color_rgb}, Color name: {color_name}")
            
            return dominant_color_rgb.tolist(), color_name
        
        except Exception as e:
            logger.error(f"Error in extract_pill_color: {str(e)}", exc_info=True)
            return [0, 0, 0], ("알 수 없음", "알 수 없음")

    def get_color_name(self, rgb_color):
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

model = PillRecognitionModel()

@app.route('/process_image', methods=['POST'])
def process_image():
    start_time = time.time()

    if 'image' not in request.json:
        logger.error("No image data provided")
        return jsonify({'error': 'No image data provided'}), 400

    try:
        image_data = base64.b64decode(request.json['image'])
        nparr = np.frombuffer(image_data, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if image is None:
            raise ValueError("Failed to decode image")

        results = model.process_image(image)
        processing_time = time.time() - start_time
        logger.info(f"Image processed successfully in {processing_time:.2f} seconds")
        return jsonify({'results': results, 'processing_time': processing_time}), 200

    except Exception as e:
        logger.error(f"Error occurred: {str(e)}", exc_info=True)
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    try:
        logger.info('서버를 시작합니다...')
        print('서버가 http://121.132.196.27:5000 에서 실행 중입니다.')
        app.run(host='0.0.0.0', port=5000, debug=False)
    except Exception as e:
        logger.error(f"서버 실행 중 오류 발생: {str(e)}", exc_info=True)
