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

| 필드 | 타입 | 설명 |
|------|------|------|
| success | boolean | 요청 성공 여부 |
| message | string | 응답에 대한 설명 메시지 |
| data | object/array | 성공 시 반환되는 데이터 (엔드포인트에 따라 다름) |
| error | string | 실패 시 오류에 대한 상세 설명 |

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
  | 필드 | 타입 | 설명 |
  |------|------|------|
  | image | string | base64로 인코딩된 알약 이미지 데이터 |

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
  | 필드 | 타입 | 설명 |
  |------|------|------|
  | item_seq | string | 품목일련번호 |
  | item_name | string | 제품명 |
  | company_name | string | 업체명 |
  | efficacy | string | 효능  |
  | usage | string | 용법 용량 |
  | precautions_warning | string | 주의사항 경고 |
  | precautions | string | 일반 주의사항 |
  | interactions | string | 상호작용 |
  | side_effects | string | 부작용 |
  | storage | string | 보관 방법 |
  | image_url | string | 제품 이미지 URL |
  | print_front | string | 식별문자코드(앞) |
  | print_back | string | 식별문자코드(뒤) |
  | color | string | 알약 색상 |
  | shape | string | 알약 모양 |

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
  | 필드 | 타입 | 설명 |
  |------|------|------|
  | userId | string | 사용자 고유 식별자 |
  | date | string | 법적 고지 수락 일시 (ISO 8601 형식) |
  | accepted | boolean | 법적 고지 수락 여부 |

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
  | 필드 | 타입 | 설명 |
  |------|------|------|
  | itemSeq | string | 품목일련번호 |
  | itemName | string | 제품명 |
  | efcyQesitm | string | 효능 효과 |
  | atpnQesitm | string | 주의사항 |
  | seQesitm | string | 부작용 |
  | etcotc | string | 전문/일반 구분 |
  | itemImage | string | 제품 이미지 URL |

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
  | 필드 | 타입 | 설명 |
  |------|------|------|
  | itemSeq | string | 품목일련번호 |
  | itemName | string | 제품명 |
  | efcyQesitm | string | 효능 효과 |
  | atpnQesitm | string | 주의사항 |
  | seQesitm | string | 부작용 |
  | etcotc | string | 전문/일반 구분 |
  | itemImage | string | 제품 이미지 URL |

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
  | 필드 | 타입 | 설명 |
  |------|------|------|
  | user_id | string | 사용자 고유 식별자 |
  | itemSeq | string | 품목일련번호 |
  | itemName | string | 제품명 |
  | efcyQesitm | string | 효능 효과 |
  | atpnQesitm | string | 주의사항 |
  | seQesitm | string | 부작용 |
  | etcotc | string | 전문/일반 구분 |
  | itemImage | string | 제품 이미지 URL |

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
  | 필드 | 타입 | 설명 |
  |------|------|------|
  | itemSeq | string | 삭제할 알약의 품목일련번호 |
  | user_id | string | 사용자 고유 식별자 |

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
    "age": 30,
    "gender": "string",
    "pregnant": false,
    "nursing": false,
    "allergy": "string"
  }
  ```
  | 필드 | 타입 | 설명 |
  |------|------|------|
  | userId | string | 사용자 고유 식별자 |
  | age | integer | 사용자 나이 |
  | gender | string | 사용자 성별 |
  | pregnant | boolean | 임신 여부 |
  | nursing | boolean | 수유 여부 |
  | allergy | string | 알레르기 정보 |

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
  | 필드 | 타입 | 설명 |
  |------|------|------|
  | userId | string | 사용자 고유 식별자 |

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
  | 필드 | 타입 | 설명 |
  |------|------|------|
  | noneItemName | string | 상호작용 약물명 |
  | noneIngrName | string | 상호작용 약물 성분명 |
  | noneItemImage | string | 상호작용 약물 이미지 URL |

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
      "age": 30,
      "gender": "string",
      "pregnant": false,
      "nursing": false,
      "allergy": "string"
    }
  }
  ```
  | 필드 | 타입 | 설명 |
  |------|------|------|
  | age | integer | 사용자 나이 |
  | gender | string | 사용자 성별 |
  | pregnant | boolean | 임신 여부 |
  |
