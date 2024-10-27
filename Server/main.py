## identify_and_get_pill_info(pill_result) 연구하기 ### 10월 27일
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

# IP 주소
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
    
def get_most_similar_pills(text, results, limit=5, threshold=60):
    """
    텍스트 유사도를 기준으로 가장 비슷한 알약을 찾는 함수
    threshold: 최소 유사도 점수 (%)
    """
    scored_results = []
    for result in results:
        front_text = result['PRINT_FRONT'] or ''
        back_text = result['PRINT_BACK'] or ''
        
        front_ratio = fuzz.ratio(text, front_text)
        back_ratio = fuzz.ratio(text, back_text)
        max_ratio = max(front_ratio, back_ratio)
        
        if max_ratio >= threshold:  # 임계값 이상인 결과만 포함
            scored_results.append((max_ratio, result))
    
    scored_results.sort(reverse=True, key=lambda x: x[0])
    return [result for score, result in scored_results[:limit]]
    
# 모델서버로부터 받은 알약 식별 정보로 알약 상세정보 조회 함수
def calculate_text_similarity(text1, text2):
    """
    텍스트 유사도를 계산하는 함수
    1. 전체 텍스트 유사도
    2. 단어 단위 유사도
    3. 문자 단위 유사도를 모두 고려
    """
    if not text1 or not text2:
        return 0
    
    # 기본 전처리
    text1 = text1.strip().upper()
    text2 = text2.strip().upper()
    
    # 1. 전체 텍스트 유사도
    full_ratio = fuzz.ratio(text1, text2)
    
    # 2. 단어 단위 유사도
    words1 = set(text1.split())
    words2 = set(text2.split())
    if words1 and words2:
        word_ratio = len(words1 & words2) * 100 / max(len(words1), len(words2))
    else:
        word_ratio = 0
    
    # 3. 부분 문자열 매칭
    substring_ratio = 0
    for length in range(min(len(text1), len(text2)), 1, -1):
        for i in range(len(text1) - length + 1):
            substr = text1[i:i+length]
            if substr in text2:
                substring_ratio = (length * 100) / max(len(text1), len(text2))
                break
        if substring_ratio > 0:
            break
    
    # 가중치를 적용한 최종 점수
    return max(
        full_ratio * 1.0,      # 완전 일치에 가장 높은 가중치
        word_ratio * 0.8,      # 단어 단위 일치
        substring_ratio * 0.6   # 부분 문자열 일치
    )

def check_color_match(input_color, db_color):
    """
    색상 그룹 매칭을 확인하는 함수
    입력된 색상이 DB의 색상 그룹에 포함되는지 확인
    """
    # 노랑/주황/분홍/빨강/갈색 그룹
    warm_colors = ['노랑', '주황', '분홍', '빨강', '갈색']
    # 연두/초록/청록 그룹
    green_colors = ['연두', '초록', '청록']
    # 파랑/남색 그룹
    blue_colors = ['파랑', '남색']
    # 자주/보라 그룹
    purple_colors = ['자주', '보라']

    if input_color == "노랑/주황/분홍/빨강/갈색" and db_color in warm_colors:
        return True
    elif input_color == "연두/초록/청록" and db_color in green_colors:
        return True
    elif input_color == "파랑/남색" and db_color in blue_colors:
        return True
    elif input_color == "자주/보라" and db_color in purple_colors:
        return True
    elif input_color == db_color:  # 하양, 검정, 회색 등 단일 색상
        return True
    return False


def identify_and_get_pill_info(pill_result):
    """
    알약 식별 및 정보 검색을 위한 통합 함수
    """
    logger.info("Starting integrated pill identification and info retrieval")
    
    text = ' '.join(pill_result.get('text', [])).strip()
    color_info = pill_result.get('color', {})
    color_name = color_info.get('name', '').strip()  # 색상 그룹명 사용
    specific_color = color_info.get('specific', '').strip()
    shape_info = pill_result.get('shape', {})
    shape = shape_info.get('predicted_class', '').strip() if isinstance(shape_info, dict) else ''
    
    logger.info(f"\n입력 데이터:")
    logger.info(f"- 텍스트: {text}")
    logger.info(f"- 색상 그룹: {color_name}")
    logger.info(f"- 구체적 색상: {specific_color}")
    logger.info(f"- 모양: {shape}")

    # 첫 번째 단계: 텍스트 정확 일치
    if text:
        exact_query = """
        SELECT pi.itemSeq, pi.itemName, pi.entpName, pi.efcyQesitm, pi.useMethodQesitm, 
               pi.atpnWarnQesitm, pi.atpnQesitm, pi.intrcQesitm, pi.seQesitm, 
               pi.depositMethodQesitm, pi.itemImage,
               pid.PRINT_FRONT, pid.PRINT_BACK, pid.COLOR_CLASS1, pid.DRUG_SHAPE
        FROM pill_identification pid
        JOIN pill_information pi ON pid.ITEM_SEQ = pi.itemSeq
        WHERE (pid.PRINT_FRONT = %s OR pid.PRINT_BACK = %s)
        """
        exact_results = db_query(exact_query, (text, text))
        
        if exact_results:
            logger.info("\n텍스트 매칭 결과:")
            scored_results = []
            for result in exact_results:
                front_text = result.get('PRINT_FRONT', '') or ''
                back_text = result.get('PRINT_BACK', '') or ''
                text_score = max(fuzz.ratio(text, front_text), fuzz.ratio(text, back_text))
                
                # 색상과 모양 점수
                color_score = 25 if check_color_match(color_name, result['COLOR_CLASS1']) else 0
                shape_score = 15 if result['DRUG_SHAPE'] == shape else 0
                
                total_score = text_score * 0.6 + color_score + shape_score
                
                scored_results.append((total_score, result))
                logger.info(f"\n약품명: {result['itemName']}")
                logger.info(f"- 텍스트 비교:")
                logger.info(f"  입력: {text}")
                logger.info(f"  DB 앞면: {front_text}")
                logger.info(f"  DB 뒷면: {back_text}")
                logger.info(f"  텍스트 유사도: {text_score}%")
                logger.info(f"- 색상 점수: {color_score}점 (DB: {result['COLOR_CLASS1']}, 입력: {color_name})")
                logger.info(f"- 모양 점수: {shape_score}점 (DB: {result['DRUG_SHAPE']}, 입력: {shape})")
                logger.info(f"- 총점: {total_score:.1f}점")
            
            # 점수순으로 정렬하고 상위 5개 선택 (최소 50점 이상)
            scored_results.sort(reverse=True, key=lambda x: x[0])
            filtered_results = [result for score, result in scored_results if score >= 50][:5]
            
            if filtered_results:
                return process_results(filtered_results)

    # 두 번째 단계: 텍스트 유사도 + 색상/모양
    if text and (color_name or shape):
        conditions = []
        params = []
        
        # 기본 텍스트 검색 조건
        conditions.append("(pid.PRINT_FRONT LIKE %s OR pid.PRINT_BACK LIKE %s)")
        params.extend([f"%{text}%", f"%{text}%"])
        
        # 색상과 모양 조건 추가
        if color_name:
            conditions.append("pid.COLOR_CLASS1 LIKE %s")
            params.append(f"%{color_name}%")
        if shape:
            conditions.append("pid.DRUG_SHAPE = %s")
            params.append(shape)
            
        query = f"""
        SELECT pi.itemSeq, pi.itemName, pi.entpName, pi.efcyQesitm, pi.useMethodQesitm, 
               pi.atpnWarnQesitm, pi.atpnQesitm, pi.intrcQesitm, pi.seQesitm, 
               pi.depositMethodQesitm, pi.itemImage,
               pid.PRINT_FRONT, pid.PRINT_BACK, pid.COLOR_CLASS1, pid.DRUG_SHAPE
        FROM pill_identification pid
        JOIN pill_information pi ON pid.ITEM_SEQ = pi.itemSeq
        WHERE {" AND ".join(conditions)}
        """
        
        results = db_query(query, tuple(params))
        if results:
            logger.info("\n텍스트 + 색상/모양 매칭 결과:")
            for result in results:
                logger.info(f"\n약품명: {result['itemName']}")
                logger.info(f"- 텍스트: {result['PRINT_FRONT']} / {result['PRINT_BACK']}")
                logger.info(f"- 색상: {result['COLOR_CLASS1']}")
                logger.info(f"- 모양: {result['DRUG_SHAPE']}")
            return process_results(results[:5])

    # 세 번째 단계: 색상/모양 매칭
    if color_name or shape:
        conditions = []
        params = []
        
        if color_name:
            conditions.append("pid.COLOR_CLASS1 LIKE %s")
            params.append(f"%{color_name}%")
        if shape:
            conditions.append("pid.DRUG_SHAPE = %s")
            params.append(shape)
            
        if conditions:
            query = f"""
            SELECT pi.itemSeq, pi.itemName, pi.entpName, pi.efcyQesitm, pi.useMethodQesitm, 
                   pi.atpnWarnQesitm, pi.atpnQesitm, pi.intrcQesitm, pi.seQesitm, 
                   pi.depositMethodQesitm, pi.itemImage,
                   pid.PRINT_FRONT, pid.PRINT_BACK, pid.COLOR_CLASS1, pid.DRUG_SHAPE
            FROM pill_identification pid
            JOIN pill_information pi ON pid.ITEM_SEQ = pi.itemSeq
            WHERE {" AND ".join(conditions)}
            LIMIT 5
            """
            
            results = db_query(query, tuple(params))
            
            if results:
                logger.info("\n색상/모양 매칭 결과:")
                for result in results:
                    logger.info(f"\n약품명: {result['itemName']}")
                    logger.info(f"- 색상: {result['COLOR_CLASS1']}")
                    logger.info(f"- 모양: {result['DRUG_SHAPE']}")
                return process_results(results)
    
    logger.warning("\n매칭되는 결과를 찾을 수 없습니다.")
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
    
    query = f"SELECT * FROM normal_drug{where_clause}"
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
    FROM normal__drug 
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
    user_id = request.args.get('userId')

    if not drug_item_name or not user_id:
        return create_response(False, "Missing parameter", error="drugItemName and userId are required")

    # 사용자 복용 중인 약물 가져오기
    user_pills_query = "SELECT itemSeq, itemName FROM user_pill WHERE user_id = %s"
    user_pills = db_query(user_pills_query, (user_id,))
    user_pill_names = [pill['itemName'] for pill in user_pills]  # 사용자가 복용 중인 약물 목록

    # 복용 중인 약물이 없으면 빈 결과 반환
    if not user_pill_names:
        return create_response(True, "No drug interactions found", data=[])

    # 복용 중인 약물 목록을 문자열로 변환 (SQL의 IN 절에서 사용할 수 있도록)
    # '%s' 형식을 사용하여 SQL에 안전하게 값을 전달
    formatted_user_pill_names = ', '.join(['%s'] * len(user_pill_names))

    # 상호작용 약물 조회 (복용 중인 약물에 대한 상호작용만 조회)
    interaction_query = f"""
    SELECT noneItemName, noneIngrName, noneItemImage 
    FROM none_drug 
    WHERE drugItemName = %s AND noneItemName IN ({formatted_user_pill_names})
    """
    
    # 매개변수로 drug_item_name과 user_pill_names의 값을 전달
    interactions = db_query(interaction_query, [drug_item_name] + user_pill_names)

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
