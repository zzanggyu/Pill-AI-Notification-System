from flask import Flask, jsonify, request
from flask_cors import CORS
import pymysql

app = Flask(__name__)
CORS(app)  # CORS 설정을 추가하여 외부 접근을 허용

# 데이터베이스 연결을 생성하는 함수
def get_db_connection():
    return pymysql.connect(
        host='localhost',
        port=3306,
        user='root',
        password='1234',
        database='pill2',
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor
    )

# 테이블을 생성하는 함수
def create_tables():
    db = get_db_connection()
    with db.cursor() as cursor:
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
        db.commit()
        
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
        db.commit()

    db.close()

# 기본 엔드포인트
@app.route('/')
def index():
    return "Pill API Service is running!"

# 법적 고지 엔드포인트
@app.route('/legal-notice', methods=['POST'])
def legal_notice():
    try:
        # 클라이언트에서 전송한 JSON 데이터를 파싱
        data = request.get_json()
        user_id = data.get('userId')
        date = data.get('date')
        accepted = data.get('accepted')

        if user_id is None or date is None or accepted is None:
            return jsonify({"error": "Invalid data"}), 400

        db = get_db_connection()
        with db.cursor() as cursor:
            # 기존에 해당 user_id가 있는지 확인
            cursor.execute("SELECT * FROM legal_notices WHERE user_id = %s", (user_id,))
            result = cursor.fetchone()

            if result:
                # 이미 존재하는 경우
                return jsonify({"message": "User already accepted legal notice"}), 200

            # 데이터 삽입
            insert_query = """
            INSERT INTO legal_notices (user_id, date, accepted) 
            VALUES (%s, %s, %s)
            """
            cursor.execute(insert_query, (user_id, date, accepted))
            db.commit()

        db.close()
        return jsonify({"message": "Legal notice recorded successfully"}), 201
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/api/check-legal-notice', methods=['GET'])
def check_legal_notice():
    user_id = request.args.get('userId')
    if not user_id:
        return jsonify({"error": "userId is required"}), 400

    db = get_db_connection()
    with db.cursor() as cursor:
        cursor.execute("SELECT * FROM legal_notices WHERE user_id = %s", (user_id,))
        result = cursor.fetchone()
    db.close()

    if result:
        return jsonify({"message": "User already accepted legal notice"}), 200
    else:
        return jsonify({"message": "User has not accepted legal notice"}), 404

# 기존 약물 검색 엔드포인트
@app.route('/api/pills/search', methods=['GET'])
def search():
    symptom = request.args.get('symptom', '')
    selected_symptoms = request.args.getlist('selectedSymptoms')
    query_conditions = []

    if symptom:
        query_conditions.append("efcyQesitm LIKE %s")
    if selected_symptoms:
        for s in selected_symptoms:
            query_conditions.append("efcyQesitm LIKE %s")
    where_clause = " WHERE " + " OR ".join(query_conditions) if query_conditions else ""

    try:
        db = get_db_connection()
        with db.cursor() as cursor:
            drug_query = f"SELECT * FROM only_data{where_clause}"
            parameters = ["%" + symptom + "%"] if symptom else []
            parameters += ["%" + s + "%" for s in selected_symptoms]
            cursor.execute(drug_query, parameters)
            drug_results = cursor.fetchall()

        db.close()
        print(f"Query result: {drug_results}")
        return jsonify(drug_results)
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 500

# 약 이름으로 검색하는 새로운 엔드포인트 추가
@app.route('/api/pills/searchByName', methods=['GET'])
def search_by_name():
    item_name = request.args.get('itemName', '')

    if not item_name:
        return jsonify({"error": "itemName parameter is required"}), 400

    try:
        db = get_db_connection()
        with db.cursor() as cursor:
            # 약 이름으로 검색하는 쿼리
            drug_query = """
            SELECT itemSeq, itemName, efcyQesitm, atpnQesitm, seQesitm, etcotc, itemImage 
            FROM only_data 
            WHERE itemName LIKE %s
            """
            cursor.execute(drug_query, ("%" + item_name + "%",))
            drug_results = cursor.fetchall()

        db.close()
        print(f"Query result: {drug_results}")
        return jsonify(drug_results)
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 500


@app.route('/api/pills/add', methods=['POST'])
def add_pill():
    try:
        data = request.get_json()
        print(f"Received JSON data: {data}")
        user_id = data.get('user_id')
        print(f"Received user_id: {user_id}")
        item_seq = data.get('itemSeq')
        item_name = data.get('itemName')
        efcyQesitm = data.get('efcyQesitm')
        atpnQesitm = data.get('atpnQesitm')
        seQesitm = data.get('seQesitm')
        etcotc = data.get('etcotc')
        item_image = data.get('itemImage')

        db = get_db_connection()
        with db.cursor() as cursor:
            # 테이블이 존재하지 않으면 생성
            create_table_query = """
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
            cursor.execute(create_table_query)
            db.commit()

            # 약물 정보를 삽입
            sql = """
            INSERT INTO user_pill (user_id, itemSeq, itemName, efcyQesitm, atpnQesitm, seQesitm, etcotc, itemImage) 
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            """
            cursor.execute(sql, (user_id, item_seq, item_name, efcyQesitm, atpnQesitm, seQesitm, etcotc, item_image))
            db.commit()

        db.close()
        return jsonify({"message": "Pill added successfully"}), 201
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 500


# 약물 삭제 엔드포인트
@app.route('/api/pills/delete', methods=['POST'])
def delete_pill():
    try:
        data = request.get_json()
        item_seq = data.get('itemSeq')
        user_id = data.get('user_id')

        if not item_seq or not user_id:
            return jsonify({"error": "Missing parameters"}), 400

        db = get_db_connection()
        with db.cursor() as cursor:
            sql = "DELETE FROM user_pill WHERE itemSeq = %s AND user_id = %s"
            cursor.execute(sql, (item_seq, user_id))
            db.commit()

        db.close()
        return jsonify({"message": "Pill deleted successfully"}), 200
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 500




@app.route('/api/pills/user', methods=['GET'])
def get_user_medications():
    try:
        user_id = request.args.get('user_id')

        db = get_db_connection()
        with db.cursor() as cursor:
            sql = "SELECT * FROM user_pill WHERE user_id = %s"
            cursor.execute(sql, (user_id,))
            medications = cursor.fetchall()

        db.close()
        return jsonify(medications), 200
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/api/pills/all', methods=['GET'])
def get_all_medications():
    try:
        db = get_db_connection()
        with db.cursor() as cursor:
            sql = "SELECT * FROM user_pill"
            cursor.execute(sql)
            medications = cursor.fetchall()

        db.close()
        return jsonify(medications), 200
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 500

# 개인 정보 저장 엔드포인트
@app.route('/api/personal-info/save', methods=['POST'])
def save_personal_info():
    try:
        data = request.get_json()
        user_id = data.get('userId')
        age = data.get('age')
        gender = data.get('gender')
        pregnant = data.get('pregnant')
        nursing = data.get('nursing')
        allergy = data.get('allergy')

        db = get_db_connection()
        with db.cursor() as cursor:
            # legal_notices 테이블에 정보 업데이트
            update_query = """
            UPDATE legal_notices
            SET age = %s, gender = %s, pregnant = %s, nursing = %s, allergy = %s
            WHERE user_id = %s
            """
            cursor.execute(update_query, (age, gender, pregnant, nursing, allergy, user_id))
            db.commit()

        db.close()
        return jsonify({"message": "Personal information saved successfully"}), 200
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 500

# 개인 정보 초기화 엔드포인트
@app.route('/api/personal-info/reset', methods=['POST'])
def reset_personal_info():
    try:
        data = request.get_json()
        user_id = data.get('userId')

        db = get_db_connection()
        with db.cursor() as cursor:
            # legal_notices 테이블에서 정보 초기화
            reset_query = """
            UPDATE legal_notices
            SET age = NULL, gender = NULL, pregnant = NULL, nursing = NULL, allergy = NULL
            WHERE user_id = %s
            """
            cursor.execute(reset_query, (user_id,))
            db.commit()

        db.close()
        return jsonify({"message": "Personal information reset successfully"}), 200
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/api/getDrugInteractions', methods=['GET'])
def get_drug_interactions():
    drug_item_name = request.args.get('drugItemName')

    if not drug_item_name:
        return jsonify({"error": "drugItemName parameter is required"}), 400

    try:
        db = get_db_connection()
        with db.cursor() as cursor:
            # 상호작용 정보를 가져오는 쿼리
            query = """
            SELECT noneItemName, noneIngrName, noneItemImage 
            FROM none_drug 
            WHERE drugItemName = %s
            """
            cursor.execute(query, (drug_item_name,))
            interactions = cursor.fetchall()

        db.close()
        return jsonify(interactions), 200
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/api/personal-info', methods=['GET'])
def get_personal_info():
    user_id = request.args.get('userId')
    
    db = get_db_connection()
    with db.cursor() as cursor:
        cursor.execute("SELECT age, gender, pregnant, nursing, allergy FROM legal_notices WHERE user_id = %s", (user_id,))
        result = cursor.fetchone()

    db.close()

    if result:
        # 숫자(0, 1)를 Boolean 값으로 변환
        result['pregnant'] = bool(result['pregnant'])
        result['nursing'] = bool(result['nursing'])
        return jsonify(result), 200
    else:
        return jsonify({"error": "Personal info not found"}), 404






if __name__ == '__main__':
    create_tables()  # 테이블이 없을 경우 생성
    app.run(host='0.0.0.0', port=5000, debug=True)