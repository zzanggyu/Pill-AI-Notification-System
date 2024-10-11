# API 엔드포인트 문서

## 기본 URL
모든 엔드포인트는 다음 기본 URL에 접두됩니다: `http://[your-domain]/api/v1`

## 응답 형식
모든 API 응답은 다음 형식을 따릅니다:
```json
{
  "success": true/false,
  "message": "응답 메시지",
  "data": {}, // 성공 시 데이터 (선택적)
  "error": "" // 실패 시 오류 메시지 (선택적)
}
```

## 엔드포인트

### 1. 알약 분석
- **URL:** `/analyze_pill`
- **Method:** POST
- **설명:** 이미지를 분석하여 알약 정보를 반환합니다.
- **요청 본문:**
  ```json
  {
    "image": "base64로 인코딩된 이미지 데이터"
  }
  ```
- **성공 응답 (200 OK):**
  ```json
  {
    "success": true,
    "message": "Pills processed successfully",
    "data": [
      {
        "item_seq": "string",
        "item_name": "string",
        "company_name": "string",
        "efficacy": "string",
        "usage": "string",
        "precautions_warning": "string",
        "precautions": "string",
        "interactions": "string",
        "side_effects": "string",
        "storage": "string",
        "image_url": "string",
        "print_front": "string",
        "print_back": "string",
        "color": "string",
        "shape": "string"
      }
    ]
  }
  ```

### 2. 법적 고지 저장
- **URL:** `/legal-notice`
- **Method:** POST
- **설명:** 사용자의 법적 고지 수락 정보를 저장합니다.
- **요청 본문:**
  ```json
  {
    "userId": "string",
    "date": "YYYY-MM-DD HH:MM:SS",
    "accepted": true
  }
  ```
- **성공 응답 (200 OK):**
  ```json
  {
    "success": true,
    "message": "Legal notice recorded successfully"
  }
  ```

### 3. 법적 고지 확인
- **URL:** `/check-legal-notice`
- **Method:** GET
- **설명:** 사용자의 법적 고지 수락 여부를 확인합니다.
- **쿼리 파라미터:**
  - `userId`: 사용자 ID (필수)
- **성공 응답 (200 OK):**
  ```json
  {
    "success": true,
    "message": "User already accepted legal notice"
  }
  ```
- **실패 응답 (404 Not Found):**
  ```json
  {
    "success": false,
    "message": "User has not accepted legal notice"
  }
  ```

### 4. 알약 정보 검색 (증상 기반)
- **URL:** `/pills/search`
- **Method:** GET
- **설명:** 증상을 기반으로 알약 정보를 검색합니다.
- **쿼리 파라미터:**
  - `symptom`: 검색할 증상 (선택적)
  - `selectedSymptoms`: 선택된 증상들 (배열, 선택적)
- **성공 응답 (200 OK):**
  ```json
  {
    "success": true,
    "message": "Search completed successfully",
    "data": [
      {
        "itemSeq": "string",
        "itemName": "string",
        "efcyQesitm": "string",
        "atpnQesitm": "string",
        "seQesitm": "string",
        "etcotc": "string",
        "itemImage": "string"
      }
    ]
  }
  ```

### 5. 알약 정보 검색 (이름 기반)
- **URL:** `/pills/searchByName`
- **Method:** GET
- **설명:** 알약 이름을 기반으로 정보를 검색합니다.
- **쿼리 파라미터:**
  - `itemName`: 알약 이름 (필수)
- **성공 응답 (200 OK):**
  ```json
  {
    "success": true,
    "message": "Search completed",
    "data": [
      {
        "itemSeq": "string",
        "itemName": "string",
        "efcyQesitm": "string",
        "atpnQesitm": "string",
        "seQesitm": "string",
        "etcotc": "string",
        "itemImage": "string"
      }
    ]
  }
  ```

### 6. 알약 추가
- **URL:** `/pills/add`
- **Method:** POST
- **설명:** 사용자의 알약 정보를 추가합니다.
- **요청 본문:**
  ```json
  {
    "user_id": "string",
    "itemSeq": "string",
    "itemName": "string",
    "efcyQesitm": "string",
    "atpnQesitm": "string",
    "seQesitm": "string",
    "etcotc": "string",
    "itemImage": "string"
  }
  ```
- **성공 응답 (201 Created):**
  ```json
  {
    "success": true,
    "message": "Pill added successfully"
  }
  ```

### 7. 알약 삭제
- **URL:** `/pills/delete`
- **Method:** POST
- **설명:** 사용자의 알약 정보를 삭제합니다.
- **요청 본문:**
  ```json
  {
    "itemSeq": "string",
    "user_id": "string"
  }
  ```
- **성공 응답 (200 OK):**
  ```json
  {
    "success": true,
    "message": "Pill deleted successfully"
  }
  ```

### 8. 개인 정보 저장
- **URL:** `/personal-info/save`
- **Method:** POST
- **설명:** 사용자의 개인 정보를 저장합니다.
- **요청 본문:**
  ```json
  {
    "userId": "string",
    "age": int,
    "gender": "string",
    "pregnant": boolean,
    "nursing": boolean,
    "allergy": "string"
  }
  ```
- **성공 응답 (200 OK):**
  ```json
  {
    "success": true,
    "message": "Personal information saved successfully"
  }
  ```

### 9. 개인 정보 초기화
- **URL:** `/personal-info/reset`
- **Method:** POST
- **설명:** 사용자의 개인 정보를 초기화합니다.
- **요청 본문:**
  ```json
  {
    "userId": "string"
  }
  ```
- **성공 응답 (200 OK):**
  ```json
  {
    "success": true,
    "message": "Personal information reset successfully"
  }
  ```

### 10. 약물 상호작용 정보 조회
- **URL:** `/getDrugInteractions`
- **Method:** GET
- **설명:** 특정 약물의 상호작용 정보를 조회합니다.
- **쿼리 파라미터:**
  - `drugItemName`: 약물 이름 (필수)
- **성공 응답 (200 OK):**
  ```json
  {
    "success": true,
    "message": "Drug interactions retrieved",
    "data": [
      {
        "noneItemName": "string",
        "noneIngrName": "string",
        "noneItemImage": "string"
      }
    ]
  }
  ```

### 11. 개인 정보 조회
- **URL:** `/personal-info`
- **Method:** GET
- **설명:** 사용자의 개인 정보를 조회합니다.
- **쿼리 파라미터:**
  - `userId`: 사용자 ID (필수)
- **성공 응답 (200 OK):**
  ```json
  {
    "success": true,
    "message": "Personal info retrieved",
    "data": {
      "age": int,
      "gender": "string",
      "pregnant": boolean,
      "nursing": boolean,
      "allergy": "string"
    }
  }
  ```
- **실패 응답 (404 Not Found):**
  ```json
  {
    "success": false,
    "message": "Personal info not found",
    "error": "User not found"
  }
  ```

## 오류 응답
모든 엔드포인트는 오류 발생 시 다음과 같은 형식의 응답을 반환합니다:
```json
{
  "success": false,
  "message": "오류 메시지",
  "error": "상세 오류 설명"
}
```

## 주의사항
1. 모든 요청에는 적절한 인증 정보가 포함되어야 합니다 (구현 예정).
2. 대용량 데이터 전송 시 적절한 페이지네이션을 사용해야 합니다.
3. 이미지 데이터는 base64로 인코딩되어 전송됩니다.
4. 모든 날짜/시간 데이터는 ISO 8601 형식을 따릅니다.
