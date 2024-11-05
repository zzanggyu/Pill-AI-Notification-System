import ssl
import requests
import pandas as pd
from flask import Flask, jsonify, request, send_from_directory
import pymysql  # MariaDB 연결용
import mysql.connector

# SSL 설정을 위한 어댑터 클래스
class SSLAdapter(requests.adapters.HTTPAdapter):
    def init_poolmanager(self, *args, **kwargs):
        context = ssl.create_default_context()
        context.set_ciphers('DEFAULT@SECLEVEL=1')
        kwargs['ssl_context'] = context
        return super().init_poolmanager(*args, **kwargs)

# Flask 앱 인스턴스 생성
app = Flask(__name__)

# 세션 생성 및 SSL 어댑터 설정
session = requests.Session()
session.mount('https://', SSLAdapter())

# 데이터베이스 연결 설정 함수
def connect_db():
    return pymysql.connect(
        host="127.0.0.1",
        user="root",
        password="1234",
        database="pill2",
        charset="utf8mb4"
    )

# 약물 정보 검색 API
@app.route('/get_combined_drug_info', methods=['GET'])
def get_combined_drug_info():
    """외부 API를 통해 약물 정보를 검색하고 반환합니다."""
    try:
        # 요청에서 검색할 약물 이름과 페이지 번호 가져오기
        item_name = request.args.get('itemName', '')
        page_no = request.args.get('pageNo', '1')
        if not item_name:
            return jsonify({"error": "itemName parameter is missing"}), 400

        # 외부 API URL 및 파라미터 설정
        url1 = 'https://apis.data.go.kr/1471000/DrbEasyDrugInfoService/getDrbEasyDrugList'
        params1 = {
            'serviceKey': '5jTQuTz5XiwyEn5FADINReaDmQGsMBzBRb6t0YJ8lovyERghjyE5aRYhPGyORIbXOVScKHfzUh5IV/pWIeKPlA==',  # 실제 서비스 키
            'pageNo': page_no,
            'numOfRows': '10',
            'itemName': item_name,
            'type': 'json'
        }

        # API 요청
        response1 = session.get(url1, params=params1)
        response1.raise_for_status()

        # JSON 데이터 처리
        data1 = response1.json()
        if 'body' in data1 and 'items' in data1['body']:
            # pandas 데이터프레임 생성 및 필터링
            df1 = pd.DataFrame(data1['body']['items'])
            if 'itemImage' not in df1.columns:
                df1['itemImage'] = ''
            else:
                df1['itemImage'] = df1['itemImage'].apply(lambda x: x if isinstance(x, str) and x else '')
            combined_df = df1[['itemName', 'itemImage']].rename(columns={'itemName': 'itemName', 'itemImage': 'imageUrl'})
        else:
            combined_df = pd.DataFrame(columns=['itemName', 'imageUrl'])

        # 결과 반환
        return jsonify(combined_df.to_dict(orient='records')), 200

    except requests.exceptions.RequestException as e:
        return jsonify({"error": f"External API request failed: {str(e)}"}), 502

    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

# 약물 호환성 검사 API
@app.route('/check_compatibility', methods=['POST'])
def check_compatibility():
    """호환성 데이터베이스를 참조하여 약물 간 호환성을 검사합니다."""
    data = request.json
    medication_names = data.get('medicationNames', [])

    # 수신된 약물 이름 로그 추가
    print(f"Received medication names: {medication_names}")

    if not medication_names:
        return jsonify({"error": "medicationNames parameter is missing"}), 400

    connection = connect_db()
    cursor = connection.cursor()

    # 호환성 검사 쿼리
    query = """
        SELECT drugItemName, noneItemName 
        FROM none_drug 
        WHERE (drugItemName IN %s AND noneItemName IN %s)
           OR (noneItemName IN %s AND drugItemName IN %s)
    """
    cursor.execute(query, (tuple(medication_names), tuple(medication_names),
                           tuple(medication_names), tuple(medication_names)))
    result = cursor.fetchall()
    connection.close()	

    if not result:
        return jsonify({"status": "compatible", "incompatible_drugs": []}), 200

    incompatible_drugs = [{"drug": row[0], "incompatible_with": row[1]} for row in result]
    return jsonify({"status": "incompatible", "incompatible_drugs": incompatible_drugs}), 200

# 모든 약물 정보를 가져오는 API
@app.route('/getAllMedications', methods=['GET'])
def get_all_medications():
    """데이터베이스에서 모든 약물 정보를 가져옵니다."""
    try:
        connection = connect_db()
        cursor = connection.cursor()
        cursor.execute("SELECT * FROM medications")
        medications = cursor.fetchall()
        connection.close()

        # JSON 형식으로 변환
        medication_list = [{"id": med[0], "itemName": med[1], "efficacy": med[2], "imageUrl": med[3], "groupId": med[4]} for med in medications]
        print("Fetched medications: ", medication_list)  # 로그 추가
        return jsonify(medication_list), 200

    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

# 이미지 파일을 제공하는 엔드포인트
@app.route('/get_image/<path:filename>', methods=['GET'])
def get_image(filename):
    """이미지 파일을 클라이언트에 제공하는 엔드포인트."""
    return send_from_directory('images', filename)

# 그룹 추가 API
@app.route('/add_group', methods=['POST'])
def add_group():
    """새로운 그룹을 데이터베이스에 추가합니다."""
    data = request.json
    group_name = data.get('groupName')
    
    if not group_name:
        return jsonify({"error": "groupName parameter is missing"}), 400

    try:
        connection = connect_db()
        cursor = connection.cursor()
        cursor.execute("INSERT INTO groups (groupName) VALUES (%s)", (group_name,))
        connection.commit()
        connection.close()
        
        return jsonify({"status": "Group added successfully"}), 201
    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

# 그룹 삭제 API
@app.route('/delete_group', methods=['DELETE'])
def delete_group():
    """데이터베이스에서 그룹을 삭제합니다."""
    group_id = request.args.get('groupId')
    
    if group_id is None or group_id == '':
        return jsonify({"error": "groupId parameter is missing"}), 400

    try:
        group_id = int(group_id)
        connection = connect_db()
        cursor = connection.cursor()
        cursor.execute("DELETE FROM groups WHERE groupId = %s", (group_id,))
        connection.commit()
        connection.close()
        
        return jsonify({"status": "Group deleted successfully"}), 200
    except ValueError:
        return jsonify({"error": "Invalid groupId format. It should be an integer."}), 400
    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

# 그룹 이름 변경 API
@app.route('/rename_group', methods=['PUT'])
def rename_group():
    """데이터베이스에서 그룹 이름을 변경합니다."""
    data = request.get_json()
    group_id = data.get('groupId')
    new_name = data.get('newName')

    if not group_id or not new_name:
        return jsonify({"error": "groupId or newName parameter is missing"}), 400

    try:
        connection = connect_db()
        cursor = connection.cursor()
        cursor.execute("UPDATE groups SET groupName = %s WHERE groupId = %s", (new_name, group_id))
        connection.commit()
        connection.close()
        
        return jsonify({"status": "Group renamed successfully"}), 200
    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

# 그룹 목록 조회 API
@app.route('/get_groups', methods=['GET'])
def get_groups():
    """데이터베이스에서 모든 그룹을 조회합니다."""
    try:
        connection = connect_db()
        cursor = connection.cursor()
        cursor.execute("SELECT * FROM groups")
        rows = cursor.fetchall()
        groups = [{"groupId": row[0], "groupName": row[1]} for row in rows]
        connection.close()
        return jsonify(groups), 200
    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

# 특정 그룹에 약물 추가 API
@app.route('/add_medication_to_group', methods=['POST'])
def add_medication_to_group():
    """특정 그룹에 약물을 추가합니다."""
    data = request.json
    group_id = data.get('groupId')
    medication_name = data.get('medicationName')
    efficacy = data.get('efficacy')
    image_url = data.get('imageUrl')

    print(f"Received groupId: {group_id}")  # 추가된 로그
    if not group_id or not medication_name:
        return jsonify({"error": "groupId or medicationName parameter is missing"}), 400

    try:
        connection = connect_db()
        cursor = connection.cursor()
        cursor.execute(
            "INSERT INTO medications (groupId, itemName, efficacy, imageUrl) VALUES (%s, %s, %s, %s)",
            (group_id, medication_name, efficacy, image_url)
        )
        connection.commit()
        connection.close()

        print(f"Inserted medication into group: {group_id}, {medication_name}")  # 추가된 로그
        return jsonify({"status": "Medication added to group successfully"}), 201
    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

# 특정 그룹에서 약물 삭제 API
@app.route('/delete_medication_from_group', methods=['DELETE'])
def delete_medication_from_group():
    """특정 그룹에서 약물을 삭제합니다."""
    medication_id = request.args.get('medicationId')

    if not medication_id:
        return jsonify({"error": "medicationId parameter is missing"}), 400

    connection = connect_db()
    cursor = connection.cursor()    

    try:
        cursor.execute("DELETE FROM medications WHERE id = %s", (medication_id,))
        connection.commit()

        if cursor.rowcount > 0:
            return jsonify({"message": "Medication deleted successfully"}), 200
        else:
            return jsonify({"error": "Medication not found"}), 404
    except Exception as e:
        connection.rollback()
        return jsonify({"error": str(e)}), 500
    finally:
        cursor.close()
        connection.close()

# 특정 그룹의 약물 목록 조회 API
@app.route('/get_medications_by_group', methods=['GET'])
def get_medications_by_group():
    """특정 그룹의 모든 약물 목록을 조회합니다."""
    group_id = request.args.get('groupId')
    if not group_id:
        return jsonify({"error": "groupId parameter is missing"}), 400

    try:
        connection = connect_db()
        cursor = connection.cursor()
        cursor.execute("SELECT * FROM medications WHERE groupId = %s", (group_id,))
        medications = cursor.fetchall()
        medications_list = [{"id": med[0], "itemName": med[1], "efficacy": med[2], "imageUrl": med[3]} for med in medications]
        connection.close()
        return jsonify(medications_list), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# Flask 앱 실행
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
