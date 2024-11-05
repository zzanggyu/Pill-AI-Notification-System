import ssl
import requests
import pandas as pd
from flask import Flask, jsonify, request, send_from_directory
import pymysql
import mysql.connector

class SSLAdapter(requests.adapters.HTTPAdapter):
    def init_poolmanager(self, *args, **kwargs):
        context = ssl.create_default_context()
        context.set_ciphers('DEFAULT@SECLEVEL=1')
        kwargs['ssl_context'] = context
        return super().init_poolmanager(*args, **kwargs)

app = Flask(__name__)

session = requests.Session()
session.mount('https://', SSLAdapter())

def connect_db():
    return pymysql.connect(
        host="127.0.0.1",
        user="root",
        password="1234",
        database="pill2",
        charset="utf8mb4"
    )

@app.route('/get_combined_drug_info', methods=['GET'])
def get_combined_drug_info():
    try:
        item_name = request.args.get('itemName', '')
        page_no = request.args.get('pageNo', '1')
        if not item_name:
            return jsonify({"error": "itemName parameter is missing"}), 400

        url1 = 'https://apis.data.go.kr/1471000/DrbEasyDrugInfoService/getDrbEasyDrugList'
        params1 = {
            'serviceKey': '5jTQuTz5XiwyEn5FADINReaDmQGsMBzBRb6t0YJ8lovyERghjyE5aRYhPGyORIbXOVScKHfzUh5IV/pWIeKPlA==',  # 실제 서비스 키
            'pageNo': page_no,
            'numOfRows': '10',
            'itemName': item_name,
            'type': 'json'
        }

        response1 = session.get(url1, params=params1)
        response1.raise_for_status()

        data1 = response1.json()
        if 'body' in data1 and 'items' in data1['body']:
            df1 = pd.DataFrame(data1['body']['items'])
            # 이미지 URL 필드 추가
            if 'itemImage' not in df1.columns:
                df1['itemImage'] = ''
            else:
                # 이미지를 포함하는 데이터프레임을 만듭니다.
                df1['itemImage'] = df1['itemImage'].apply(lambda x: x if isinstance(x, str) and x else '')

            # 필요한 필드만 선택하여 반환
            combined_df = df1[['itemName', 'itemImage']].rename(columns={'itemName': 'itemName', 'itemImage': 'imageUrl'})
        else:
            combined_df = pd.DataFrame(columns=['itemName', 'imageUrl'])

        return jsonify(combined_df.to_dict(orient='records')), 200

    except requests.exceptions.RequestException as e:
        return jsonify({"error": f"External API request failed: {str(e)}"}), 502

    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

@app.route('/check_compatibility', methods=['POST'])
def check_compatibility():
    data = request.json
    medication_names = data.get('medicationNames', [])
    

    # 수신된 약물 이름 로그 추가
    print(f"Received medication names: {medication_names}")

    if not medication_names:
        return jsonify({"error": "medicationNames parameter is missing"}), 400

    connection = connect_db()
    cursor = connection.cursor()

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

@app.route('/getAllMedications', methods=['GET'])
def get_all_medications():
    try:
        connection = connect_db()
        cursor = connection.cursor()
        cursor.execute("SELECT * FROM medications")  # 모든 약물 가져오기
        medications = cursor.fetchall()
        connection.close()

        # 약물 정보를 JSON 형태로 변환
        
        medication_list = [{"id": med[0], "itemName": med[1], "efficacy": med[2], "imageUrl": med[3], "groupId": med[4]} for med in medications]
        print("Fetched medications: ", medication_list)  # 로그 추가
        return jsonify(medication_list), 200

    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500



@app.route('/get_image/<path:filename>', methods=['GET'])
def get_image(filename):
    return send_from_directory('images', filename)

@app.route('/add_group', methods=['POST'])
def add_group():
    data = request.json
    group_name = data.get('groupName')
    
    if not group_name:
        return jsonify({"error": "groupName parameter is missing"}), 400

    try:
        connection = connect_db()
        cursor = connection.cursor()
        
        # 그룹을 데이터베이스에 추가
        cursor.execute("INSERT INTO groups (groupName) VALUES (%s)", (group_name,))
        connection.commit()
        connection.close()
        
        return jsonify({"status": "Group added successfully"}), 201
    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

@app.route('/delete_group', methods=['DELETE'])
def delete_group():
    group_id = request.args.get('groupId')  # 쿼리 파라미터로부터 groupId 읽기
    
    # group_id가 None이거나 빈 문자열일 경우 오류 응답
    if group_id is None or group_id == '':
        return jsonify({"error": "groupId parameter is missing"}), 400

    try:
        # group_id를 int로 변환
        group_id = int(group_id)

        connection = connect_db()
        cursor = connection.cursor()
        
        # 그룹 삭제
        cursor.execute("DELETE FROM groups WHERE groupId = %s", (group_id,))
        connection.commit()
        connection.close()
        
        return jsonify({"status": "Group deleted successfully"}), 200
    except ValueError:
        return jsonify({"error": "Invalid groupId format. It should be an integer."}), 400
    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

        
@app.route('/rename_group', methods=['PUT'])
def rename_group():
    data = request.get_json()  # JSON 바디로부터 데이터 읽기
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

@app.route('/get_groups', methods=['GET'])
def get_groups():
    try:
        connection = connect_db()
        cursor = connection.cursor()
        
        # 그룹 목록 조회
        cursor.execute("SELECT * FROM groups")
        rows = cursor.fetchall()
        
        # 그룹 객체 생성
        groups = [{"groupId": row[0], "groupName": row[1]} for row in rows]
        
        connection.close()
        return jsonify(groups), 200
    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500


@app.route('/add_medication_to_group', methods=['POST'])
def add_medication_to_group():
    data = request.json
    group_id = data.get('groupId')  # 확인: 여기가 제대로 전달되고 있는지
    medication_name = data.get('medicationName')
    efficacy = data.get('efficacy')
    image_url = data.get('imageUrl')

    # 로그 추가
    print(f"Received groupId: {group_id}")  # 추가된 로그
    if not group_id or not medication_name:
        return jsonify({"error": "groupId or medicationName parameter is missing"}), 400

    try:
        connection = connect_db()
        cursor = connection.cursor()

        # 약물 추가
        cursor.execute(
            "INSERT INTO medications (groupId, itemName, efficacy, imageUrl) VALUES (%s, %s, %s, %s)",
            (group_id, medication_name, efficacy, image_url)  # groupId가 여기서 사용되고 있는지
        )
        connection.commit()
        connection.close()

        print(f"Inserted medication into group: {group_id}, {medication_name}")  # 추가된 로그
        return jsonify({"status": "Medication added to group successfully"}), 201
    except Exception as e:
        print(f"Error occurred: {str(e)}")
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500

@app.route('/delete_medication_from_group', methods=['DELETE'])
def delete_medication_from_group():
    # 요청에서 약물 ID 가져오기
    medication_id = request.args.get('medicationId')

    if not medication_id:
        return jsonify({"error": "medicationId parameter is missing"}), 400

    connection = connect_db()
    cursor = connection.cursor()    

    try:
        # 약물 삭제 SQL 쿼리 실행
        cursor.execute("DELETE FROM medications WHERE id = %s", (medication_id,))
        connection.commit()  # 변경 사항 저장

        # 삭제된 행 수 확인
        if cursor.rowcount > 0:
            return jsonify({"message": "Medication deleted successfully"}), 200  # 성공 메시지 반환
        else:
            return jsonify({"error": "Medication not found"}), 404  # 약물이 없을 경우

    except Exception as e:
        connection.rollback()  # 오류 발생 시 롤백
        return jsonify({"error": str(e)}), 500  # 오류 메시지 반환

    finally:
        cursor.close()  # 커서 닫기
        connection.close()  # 연결 닫기







@app.route('/get_medications_by_group', methods=['GET'])
def get_medications_by_group():
    group_id = request.args.get('groupId')
    if not group_id:
        return jsonify({"error": "groupId parameter is missing"}), 400

    try:
        connection = connect_db()
        cursor = connection.cursor()
        cursor.execute("SELECT * FROM medications WHERE groupId = %s", (group_id,))
        medications = cursor.fetchall()

        # 결과를 Medication 객체로 변환하는 로직을 추가
        medications_list = [{"id": med[0], "itemName": med[1], "efficacy": med[2], "imageUrl": med[3]} for med in medications]

        connection.close()
        return jsonify(medications_list), 200  # medications_list를 반환
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/get_medications_by_group_id', methods=['GET'])
def get_medications_by_group_id():
    group_id = request.args.get('groupId')
    cursor.execute("SELECT id, itemName, efficacy, imageUrl FROM medications WHERE groupId = %s", (group_id,))
    medications = cursor.fetchall()
    print(f"Retrieved medications: {medications}")  # 로그 추가
    return jsonify(medications)


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
