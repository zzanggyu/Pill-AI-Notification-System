#### 10 월 20일 업데이트
# 필요한 라이브러리 및 모듈 임포트
import mysql.connector  # MySQL 데이터베이스 연결을 위한 라이브러리
from mysql.connector import Error
from flask import Flask, request, jsonify, Blueprint  # Flask 웹 프레임워크
import requests  # HTTP 요청을 위한 라이브러리
from flask_cors import CORS  # Cross-Origin Resource Sharing 지원
import base64  # 이미지 데이터 인코딩/디코딩
import time
import logging
from functools import wraps
from flask import Blueprint
from fuzzywuzzy import fuzz
import itertools

# 상수 정의
PUBLIC_IP = "121.132.196.27"
MODEL_SERVER_URL = f"http://{PUBLIC_IP}:5000/process_image"  # 모델 서버 URL

# v1 API (버전 1) Blueprint 생성
api_v1 = Blueprint('api_v1', __name__, url_prefix='/api/v1')

# Flask 애플리케이션 생성 및 CORS 설정
app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})  # 모든 도메인에서의 CORS 요청 허용

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# 데이터베이스 설정
DB_CONFIG = {
    'host': 'localhost',  # 호스트 설정
    'database': 'pill2',  # 데이터베이스 이름
    'user': 'root',  # 권한이 부여된 사용자 아이디
    'password': '0000'  # 권한이 부여된 사용자 비밀번호
}

# 데이터베이스 연결 풀 생성
db_pool = mysql.connector.pooling.MySQLConnectionPool(
    pool_name="mypool",
    pool_size=5,
    **DB_CONFIG
)

# 데이터베이스 연결을 가져오는 함수
def get_db_connection():
    return db_pool.get_connection()

# 데이터베이스 쿼리를 실행하는 함수
# db_query 함수 내의 로깅 수정
def db_query(query, params=None):
    connection = get_db_connection()
    try:
        with connection.cursor(dictionary=True) as cursor:
            if params:
                cursor.execute(query, params)
            else:
                cursor.execute(query)
            
            if query.strip().upper().startswith("SELECT"):
                result = cursor.fetchall()
            else:
                connection.commit()
                result = cursor.rowcount
            
            # 쿼리 실행 결과 로깅 제거
            return result
    except Error as e:
        logger.error(f"Database error: {e}")
        raise
    finally:
        connection.close()

# 표준화된 API 응답을 만들기 위한 함수
def create_response(success, message, data=None, error=None):
    response = {
        "success": success,
        "message": message
    }
    if data is not None:
        response["data"] = data
    if error is not None:
        response["error"] = error
    return jsonify(response), 200 if success else 400

# 오류 처리를 위한 데코레이터
def error_handler(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        try:
            return f(*args, **kwargs)
        except Exception as e:
            logger.error(f"Error in {f.__name__}: {str(e)}", exc_info=True)
            return create_response(False, "An unexpected error occurred", error=str(e))
    return decorated_function

# 테이블 생성 함수
def create_tables():
    connection = get_db_connection()
    try:
        cursor = connection.cursor()
        
        # legal_notices 테이블 생성
        create_legal_notices_table = """
        CREATE TABLE IF NOT EXISTS legal_notices (
            user_id VARCHAR(255) PRIMARY KEY,
            date DATETIME,
            accepted BOOLEAN,
            age INT,
            gender VARCHAR(10),
            pregnant BOOLEAN,
            nursing BOOLEAN,
            allergy TEXT,
            createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        """
        cursor.execute(create_legal_notices_table)
        
        # user_pill 테이블 생성
        create_user_pill_table = """
        CREATE TABLE IF NOT EXISTS user_pill (
            user_id VARCHAR(255),
            itemSeq VARCHAR(50),
            itemName VARCHAR(255),
            efcyQesitm TEXT,
            atpnQesitm TEXT,
            seQesitm TEXT,
            etcotc VARCHAR(50),
            itemImage TEXT,
            createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        """
        cursor.execute(create_user_pill_table)
        
        connection.commit()
        logger.info("Tables created successfully")
    except Error as e:
        logger.error(f"Error creating tables: {e}")
    finally:
        cursor.close()
        connection.close()

# 알약 분석 엔드포인트

@api_v1.route('/analyze_pill', methods=['POST'])
@error_handler
def analyze_pill():
    logger.info("Starting pill analysis")
    
    if 'image' not in request.json:
        return create_response(False, "No image data provided", error="Missing image data")

    image_data = request.json['image']

    try:
        # 이미지 데이터 유효성 검사
        image_data = base64.b64encode(base64.b64decode(image_data)).decode('utf-8')
    except:
        return create_response(False, "Invalid image data format", error="Invalid image encoding")
    
    try:
        # 모델 서버에 이미지 분석 요청
        model_response = requests.post(MODEL_SERVER_URL, json={'image': image_data}, timeout=300)
        model_response.raise_for_status()
        model_results = model_response.json().get('results', [])

        final_results = []
        for pill_result in model_results:
            pill_infos = identify_and_get_pill_info(pill_result)
            if pill_infos:
                final_results.extend(pill_infos)

        if not final_results:
            return create_response(False, "No pills were successfully processed", error="Processing failed")

        return create_response(True, "Pills processed successfully", data=final_results)
        
    except Exception as e:
        logger.error(f"Error in analyze_pill: {str(e)}", exc_info=True)
        return create_response(False, "Unexpected error occurred", error=str(e))
    
# 모델서버로부터 받은 알약 식별 정보로 알약 상세정보 조회 함수
def identify_and_get_pill_info(pill_result):
    logger.info("Starting pill identification")
    
    text = ' '.join(pill_result.get('text', [])).strip()
    color_info = pill_result.get('color', {})
    rgb = color_info.get('rgb', [0, 0, 0])
    shape = pill_result.get('shape', '').strip()
    color_name = color_info.get('name', '')  # 모델 서버에서 제공하는 색상 이름 사용
    
    logger.info(f"Pill characteristics - Text: {text}, RGB: {rgb}, Color: {color_name}, Shape: {shape}")

    # 첫 번째 단계: 텍스트 기반 검색
    text_query = """
    SELECT pi.itemSeq, pi.itemName, pi.entpName, pi.efcyQesitm, pi.useMethodQesitm, 
           pi.atpnWarnQesitm, pi.atpnQesitm, pi.intrcQesitm, pi.seQesitm, 
           pi.depositMethodQesitm, pi.itemImage,
           pid.PRINT_FRONT, pid.PRINT_BACK, pid.COLOR_CLASS1, pid.DRUG_SHAPE
    FROM pill_identification pid
    JOIN pill_information pi ON pid.ITEM_SEQ = pi.itemSeq
    WHERE pid.PRINT_FRONT LIKE %s OR pid.PRINT_BACK LIKE %s
    LIMIT 10
    """
    text_params = (f"%{text}%", f"%{text}%")
    
    text_results = db_query(text_query, text_params)
    
    if text_results:
        logger.info(f"Found {len(text_results)} pills matching text")
        return process_results(text_results)
    
    # 두 번째 단계: 색상 기반 검색
    color_query = """
    SELECT pi.itemSeq, pi.itemName, pi.entpName, pi.efcyQesitm, pi.useMethodQesitm, 
           pi.atpnWarnQesitm, pi.atpnQesitm, pi.intrcQesitm, pi.seQesitm, 
           pi.depositMethodQesitm, pi.itemImage,
           pid.PRINT_FRONT, pid.PRINT_BACK, pid.COLOR_CLASS1, pid.DRUG_SHAPE
    FROM pill_identification pid
    JOIN pill_information pi ON pid.ITEM_SEQ = pi.itemSeq
    WHERE pid.COLOR_CLASS1 = %s
    LIMIT 10
    """
    color_params = (color_name,)
    
    color_results = db_query(color_query, color_params)
    
    if color_results:
        logger.info(f"Found {len(color_results)} pills matching color")
        return process_results(color_results)
    
    logger.warning(f"No pill found matching the criteria")
    return None


# 결과 처리 함수

def process_results(results):
    processed_results = []
    for result in results:
        processed_result = {
            'item_seq': result['itemSeq'],
            'item_name': result['itemName'],
            'company_name': result['entpName'],
            'efficacy': result['efcyQesitm'],
            'usage': result['useMethodQesitm'],
            'precautions_warning': result['atpnWarnQesitm'],
            'precautions': result['atpnQesitm'],
            'interactions': result['intrcQesitm'],
            'side_effects': result['seQesitm'],
            'storage': result['depositMethodQesitm'],
            'image_url': result['itemImage'],
            'print_front': result['PRINT_FRONT'],
            'print_back': result['PRINT_BACK'],
            'color': result['COLOR_CLASS1'],
            'shape': result['DRUG_SHAPE']
        }
        processed_results.append(processed_result)
    
    return processed_results

# 법적 고지 저장 엔드포인트
@api_v1.route('/legal-notice', methods=['POST'])
@error_handler
def legal_notice():
    data = request.get_json()
    user_id = data.get('userId')
    date = data.get('date')
    accepted = data.get('accepted')

    if user_id is None or date is None or accepted is None:
        return create_response(False, "Invalid data", error="Missing required fields")

    result = db_query("SELECT * FROM legal_notices WHERE user_id = %s", (user_id,))
    
    if result:
        return create_response(True, "User already accepted legal notice")

    insert_query = "INSERT INTO legal_notices (user_id, date, accepted) VALUES (%s, %s, %s)"
    db_query(insert_query, (user_id, date, accepted))
    
    return create_response(True, "Legal notice recorded successfully")

# 법적 고지 확인 엔드포인트
@api_v1.route('/check-legal-notice', methods=['GET'])
@error_handler
def check_legal_notice():
    user_id = request.args.get('userId')
    if not user_id:
        return create_response(False, "Missing parameter", error="userId is required")

    result = db_query("SELECT * FROM legal_notices WHERE user_id = %s", (user_id,))
    
    if result:
        return create_response(True, "User already accepted legal notice")
    else:
        return create_response(False, "User has not accepted legal notice")

# 알약 정보를 증상으로 검색하는 엔드포인트
@api_v1.route('/pills/search', methods=['GET'])
@error_handler
def search():
    symptom = request.args.get('symptom', '')
    selected_symptoms = request.args.getlist('selectedSymptoms')
    
    query_conditions = []
    parameters = []

    if symptom:
        query_conditions.append("efcyQesitm LIKE %s")
        parameters.append("%" + symptom + "%")
    if selected_symptoms:
        query_conditions.extend(["efcyQesitm LIKE %s"] * len(selected_symptoms))
        parameters.extend(["%" + s + "%" for s in selected_symptoms])

    where_clause = " WHERE " + " OR ".join(query_conditions) if query_conditions else ""
    
    query = f"SELECT * FROM only_data{where_clause}"
    drug_results = db_query(query, tuple(parameters))

    return create_response(True, "Search completed successfully", data=drug_results)

# 약 이름으로 검색하는 엔드포인트
@api_v1.route('/pills/searchByName', methods=['GET'])
@error_handler
def search_by_name():
    item_name = request.args.get('itemName', '')

    if not item_name:
        return create_response(False, "Missing parameter", error="itemName parameter is required")

    drug_query = """
    SELECT itemSeq, itemName, efcyQesitm, atpnQesitm, seQesitm, etcotc, itemImage 
    FROM only_data 
    WHERE itemName LIKE %s
    """
    drug_results = db_query(drug_query, ("%" + item_name + "%",))

    return create_response(True, "Search completed", data=drug_results)

# 약물 추가 엔드포인트
@api_v1.route('/pills/add', methods=['POST'])
@error_handler
def add_pill():
    data = request.get_json()
    required_fields = ['user_id', 'itemSeq', 'itemName', 'efcyQesitm', 'atpnQesitm', 'seQesitm', 'etcotc', 'itemImage']
    
    if not all(field in data for field in required_fields):
        return create_response(False, "Missing required fields", error="Incomplete data")

    query = """
    INSERT INTO user_pill (user_id, itemSeq, itemName, efcyQesitm, atpnQesitm, seQesitm, etcotc, itemImage) 
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
    """
    params = tuple(data[field] for field in required_fields)
    
    result = db_query(query, params)
    
    if result:
        return create_response(True, "Pill added successfully")
    else:
        return create_response(False, "Failed to add pill", error="Database error")

# 약물 삭제 엔드포인트
@api_v1.route('/pills/delete', methods=['POST'])
@error_handler
def delete_pill():
    data = request.get_json()
    item_seq = data.get('itemSeq')
    user_id = data.get('user_id')

    if not item_seq or not user_id:
        return create_response(False, "Missing parameters", error="Incomplete data")

    sql = "DELETE FROM user_pill WHERE itemSeq = %s AND user_id = %s"
    result = db_query(sql, (item_seq, user_id))

    if result:
        return create_response(True, "Pill deleted successfully")
    else:
        return create_response(False, "Failed to delete pill", error="Database error")

# id에 해당하는 약물 정보 불러오기
@api_v1.route('/pills/user', methods=['GET'])
@error_handler
def get_user_medications():
    user_id = request.args.get('user_id')
    
    if not user_id:
        return create_response(False, "user_id parameter is required", error="Incomplete data")

    query = """
    SELECT itemSeq, itemName, efcyQesitm, atpnQesitm, seQesitm, etcotc, itemImage 
    FROM user_pill 
    WHERE user_id = %s
    """
    medications = db_query(query, (user_id,))

    if medications:
        return create_response(True, "User medications retrieved successfully", data=medications)
    else:
        return create_response(False, "No medications found for this user", error="not found")

# 개인 정보 저장 엔드포인트
@api_v1.route('/personal-info/save', methods=['POST'])
@error_handler
def save_personal_info():
    data = request.get_json()
    required_fields = ['userId', 'age', 'gender', 'pregnant', 'nursing', 'allergy']
    
    if not all(field in data for field in required_fields):
        return create_response(False, "Missing required fields", error="Incomplete data")

    query = """
    UPDATE legal_notices
    SET age = %s, gender = %s, pregnant = %s, nursing = %s, allergy = %s
    WHERE user_id = %s
    """
    params = (data['age'], data['gender'], data['pregnant'], data['nursing'], data['allergy'], data['userId'])
    
    result = db_query(query, params)
    
    if result:
        return create_response(True, "Personal information saved successfully")
    else:
        return create_response(False, "Failed to save personal information", error="Database error")

# 개인 정보 초기화 엔드포인트
@api_v1.route('/personal-info/reset', methods=['POST'])
@error_handler
def reset_personal_info():
    data = request.get_json()
    user_id = data.get('userId')

    if not user_id:
        return create_response(False, "Missing userId", error="Incomplete data")

    reset_query = """
    UPDATE legal_notices
    SET age = NULL, gender = NULL, pregnant = NULL, nursing = NULL, allergy = NULL
    WHERE user_id = %s
    """
    result = db_query(reset_query, (user_id,))

    if result:
        return create_response(True, "Personal information reset successfully")
    else:
        return create_response(False, "Failed to reset personal information", error="Database error")

# 상호작용 정보를 가져오는 엔드포인트
@api_v1.route('/getDrugInteractions', methods=['GET'])
@error_handler
def get_drug_interactions():
    drug_item_name = request.args.get('drugItemName')

    if not drug_item_name:
        return create_response(False, "Missing parameter", error="drugItemName parameter is required")

    query = """
    SELECT noneItemName, noneIngrName, noneItemImage 
    FROM none_drug 
    WHERE drugItemName = %s
    """
    interactions = db_query(query, (drug_item_name,))

    return create_response(True, "Drug interactions retrieved", data=interactions)

# 개인 정보를 가져오는 엔드포인트
@api_v1.route('/personal-info', methods=['GET'])
@error_handler
def get_personal_info():
    user_id = request.args.get('userId')
    
    query = "SELECT age, gender, pregnant, nursing, allergy FROM legal_notices WHERE user_id = %s"
    result = db_query(query, (user_id,))

    if result:
        # 숫자 값을 Boolean 값으로 변환
        result[0]['pregnant'] = bool(result[0]['pregnant'])
        result[0]['nursing'] = bool(result[0]['nursing'])
        return create_response(True, "Personal info retrieved", data=result[0])
    else:
        return create_response(False, "Personal info not found", error="User not found")

# Blueprint를 애플리케이션에 등록
app.register_blueprint(api_v1)

if __name__ == '__main__':
    create_tables()
    print(f'서버가 http://{PUBLIC_IP}:5001 에서 실행 중입니다.')
    app.run(host='0.0.0.0', port=5001, debug=False)
