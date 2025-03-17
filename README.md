# 상품 업데이트 API 문서

상품 업데이트는 크게 **상품 정보 업데이트**와 **옵션 업데이트** 두 부분으로 나뉩니다.
이미지 파일(메인 이미지, 서브 이미지, 추가 이미지는) 별도의 Multipart로 전송되며,
JSON 본문에는 이미지 처리에 관한 지침(액션, 인덱스, 이미지ID)만 포함됩니다.

---

## 1. 상품 정보 업데이트

### 개요
- **목적:** 기존 상품의 기본 정보를 업데이트합니다.
- **요청 방식:** `PATCH`

### 요청 JSON 구조

상품 업데이트 JSON은 아래와 같은 필드로 구성됩니다:

| 필드명                   | 타입             | 필수 여부 | 설명                                                                                       |
|--------------------------|------------------|-----------|--------------------------------------------------------------------------------------------|
| `productId`              | number           | 필수      | 업데이트할 상품의 ID                                                                       |
| `userId`                 | number           | 필수      | 상품 소유자의 사용자 ID                                                                    |
| `productType`            | string           | 선택      | 상품 유형 (예: `"WEDDING_SNAP"`) <br/>미전달 시 기존 값 유지                                  |
| `shootingPlace`          | string           | 선택      | 촬영 장소 (예: `"JEJU"`) <br/>미전달 시 기존 값 유지                                         |
| `title`                  | string           | 선택      | 상품 제목 <br/>미전달 시 기존 값 유지                                                        |
| `description`            | string           | 선택      | 상품 설명 <br/>미전달 시 기존 값 유지                                                        |
| `detailedInfo`           | string           | 선택      | 상세 정보 (예: 연락처, 문의 방법 등)                                                         |
| `availableSeasons`       | array(string)    | 선택      | 촬영 가능 시기 (예: `["YEAR_2025_FIRST_HALF", "YEAR_2025_SECOND_HALF"]`)                       |
| `cameraTypes`            | array(string)    | 선택      | 카메라 종류 (예: `["DIGITAL", "FILM"]`)                                                      |
| `retouchStyles`          | array(string)    | 선택      | 보정 스타일 (예: `["MODERN", "VINTAGE"]`)                                                    |
| `contactInfo`            | string           | 선택      | 연락처 정보                                                                                |
| `subImagesFinal`         | array(object)    | 선택      | 서브 이미지 처리 지침 배열                                                                   |
| `additionalImagesFinal`  | array(object)    | 선택      | 추가 이미지 처리 지침 배열                                                                   |

#### 서브 이미지 처리 지침 (`subImagesFinal`)
각 배열 요소(객체)는 다음 필드를 포함합니다:

- **`action`**: 이미지 처리 액션
    - `"KEEP"`: 기존 이미지 유지
    - `"DELETE"`: 기존 이미지 삭제
    - `"UPLOAD"`: 새 이미지 업로드
- **`index`**: 이미지 배열에서 해당 이미지의 위치 (0부터 시작)
- **`imageId`**: 기존 이미지의 ID (업로드 시 `null`)

#### 추가 이미지 처리 지침 (`additionalImagesFinal`)
각 배열 요소(객체)는 다음 필드를 포함합니다:

- **`action`**: 이미지 처리 액션
    - `"KEEP"`: 기존 이미지 유지
    - `"DELETE"`: 기존 이미지 삭제
    - `"UPLOAD"`: 새 이미지 업로드
- **`imageId`**: 기존 이미지의 ID (업로드 시 `null`)

### 예시 요청 JSON

> **주의:**
> - **메인 이미지**: 별도의 Multipart 파일(예: `mainImageFile`)로 전송
> - **서브 이미지**: Multipart 파일 4개를 `subImageFiles`로 전송
> - **추가 이미지**: Multipart 파일 5개를 `additionalImageFiles`로 전송

```json
{
  "productId": 100,
  "userId": 1,
  "productType": "WEDDING_SNAP",
  "shootingPlace": "JEJU",
  "title": "예쁜 웨딩 사진 촬영",
  "description": "신랑, 신부의 아름다운 순간을 담은 웨딩 사진",
  "detailedInfo": "연락처: 010-1234-5678, 상세 문의는 이메일로",
  "availableSeasons": [
    "YEAR_2025_FIRST_HALF",
    "YEAR_2025_SECOND_HALF"
  ],
  "cameraTypes": [
    "DIGITAL",
    "FILM"
  ],
  "retouchStyles": [
    "MODERN",
    "VINTAGE"
  ],
  "contactInfo": "010-1234-5678",
  "subImagesFinal": [
    { "action": "UPLOAD", "index": 0, "imageId": null },
    { "action": "UPLOAD", "index": 1, "imageId": null },
    { "action": "UPLOAD", "index": 2, "imageId": null },
    { "action": "UPLOAD", "index": 3, "imageId": null }
  ],
  "additionalImagesFinal": [
    { "action": "UPLOAD", "imageId": null },
    { "action": "UPLOAD", "imageId": null },
    { "action": "UPLOAD", "imageId": null },
    { "action": "UPLOAD", "imageId": null },
    { "action": "UPLOAD", "imageId": null }
  ]
}
```

---

## 2. 옵션 업데이트

### 개요
상품의 옵션을 업데이트하는 요청입니다.
기존 옵션을 수정하고, 새로운 옵션을 추가할 수 있습니다.
옵션은 JSON 배열 형식으로 전송됩니다.

### 요청 JSON 구조 (각 옵션 객체)

| 필드명                   | 타입                | 설명                                                                                                    |
|--------------------------|---------------------|-------------------------------------------------------------------------------------------------------|
| `optionId`               | number 또는 `null`  | 기존 옵션 수정 시 옵션 ID(예: 1)을 전달 <br/>새 옵션 추가 시 `null`                                     |
| `name`                   | string              | 옵션명                                                                                                   |
| `optionType`             | string              | 옵션 유형 (예: `"SINGLE"`, `"PACKAGE"`)                                                                  |
| `discountAvailable`      | boolean             | 할인 적용 여부                                                                                           |
| `originalPrice`          | number              | 원래 가격                                                                                               |
| `discountPrice`          | number              | 할인 가격                                                                                               |
| `description`            | string              | 옵션 설명                                                                                               |
| `costumeCount`           | number              | 의상 수량 (단품 옵션의 경우 1 이상)                                                                        |
| `shootingLocationCount`  | number              | 촬영 장소 수 (단품 옵션의 경우 1 이상)                                                                     |
| `shootingHours`          | number              | 촬영 시간 (시)                                                                                          |
| `shootingMinutes`        | number              | 촬영 시간 (분)                                                                                          |
| `retouchedCount`         | number              | 보정된 사진 수 (단품 옵션의 경우 1 이상)                                                                    |
| `originalProvided`       | boolean             | 원본 제공 여부                                                                                           |
| `partnerShops`           | array(object)       | 파트너샵 목록 <br/>각 객체는 `category`, `name`, `link` 필드로 구성 (예: 헤어/메이크업 샵, 드레스 샵 등)    |

### 예시 요청 JSON

기존 옵션 1개 수정 + 새 옵션 2개 추가 시 예:

```json
[
  {
    "optionId": 1,
    "name": "옵션1 수정",
    "optionType": "SINGLE",
    "discountAvailable": false,
    "originalPrice": 100000,
    "discountPrice": 80000,
    "description": "수정된 옵션 설명",
    "costumeCount": 1,
    "shootingLocationCount": 1,
    "shootingHours": 2,
    "shootingMinutes": 30,
    "retouchedCount": 1,
    "originalProvided": true,
    "partnerShops": [
      {
        "category": "HAIR_MAKEUP",
        "name": "샘플샵",
        "link": "http://example.com"
      }
    ]
  },
  {
    "optionId": null,
    "name": "새로운 옵션 1",
    "optionType": "PACKAGE",
    "discountAvailable": true,
    "originalPrice": 150000,
    "discountPrice": 120000,
    "description": "새롭게 추가된 옵션 설명 1",
    "costumeCount": 2,
    "shootingLocationCount": 2,
    "shootingHours": 3,
    "shootingMinutes": 45,
    "retouchedCount": 2,
    "originalProvided": false,
    "partnerShops": [
      {
        "category": "DRESS",
        "name": "파트너샵1",
        "link": "http://partner1.com"
      }
    ]
  },
  {
    "optionId": null,
    "name": "새로운 옵션 2",
    "optionType": "SINGLE",
    "discountAvailable": false,
    "originalPrice": 80000,
    "discountPrice": 60000,
    "description": "새롭게 추가된 옵션 설명 2",
    "costumeCount": 1,
    "shootingLocationCount": 1,
    "shootingHours": 1,
    "shootingMinutes": 15,
    "retouchedCount": 1,
    "originalProvided": true,
    "partnerShops": [
      {
        "category": "VIDEO",
        "name": "파트너샵2",
        "link": "http://partner2.com"
      }
    ]
  }
]
```

---

## 3. 요청 시나리오 요약

1. **메인 이미지 교체**
    - `mainImageFile` (Multipart)로 전달
2. **서브 이미지 4장 업로드**
    - `subImagesFinal` 배열에 4개의 `"UPLOAD"` 액션, `imageId`는 `null`로 명시
    - 실제 파일 4개를 `subImageFiles` (Multipart)로 전송
3. **추가 이미지 5장 업로드**
    - `additionalImagesFinal` 배열에 5개의 `"UPLOAD"` 액션, `imageId`는 `null`
    - 실제 파일 5개를 `additionalImageFiles` (Multipart)로 전송
4. **옵션 수정 & 새로운 옵션 2개 추가**
    - 기존 옵션(예: ID=1)을 수정하는 객체와, `optionId=null`로 2개의 새 옵션을 추가하는 객체들을 배열로 함께 전달

---

## 4. 응답 예시

성공적으로 상품이 업데이트되면 다음과 같은 응답 JSON을 수신할 수 있습니다 (예시):

```json
{
  "productId": 100,
  "userId": 1,
  "productType": "WEDDING_SNAP",
  "shootingPlace": "JEJU",
  "title": "예쁜 웨딩 사진 촬영",
  "description": "신랑, 신부의 아름다운 순간을 담은 웨딩 사진",
  "availableSeasons": [
    "YEAR_2025_FIRST_HALF",
    "YEAR_2025_SECOND_HALF"
  ],
  "cameraTypes": [
    "DIGITAL",
    "FILM"
  ],
  "retouchStyles": [
    "MODERN",
    "VINTAGE"
  ],
  "mainImage": {
    "imageId": 9999,
    "url": "https://example.com/uploaded_main.jpg"
  },
  "subImages": [
    {
      "imageId": 10000,
      "url": "https://example.com/subImage1.jpg"
    }
    // 나머지 서브 이미지들...
  ],
  "additionalImages": [
    {
      "imageId": 20000,
      "url": "https://example.com/additional1.jpg"
    }
    // 나머지 추가 이미지들...
  ],
  "detailedInfo": "연락처: 010-1234-5678, 상세 문의는 이메일로",
  "contactInfo": "010-1234-5678",
  "createdAt": "2025-03-17T12:00:00",
  "updatedAt": "2025-03-17T12:30:00",
  "options": [
    {
      "optionId": 1,
      "productId": 100,
      "name": "옵션1 수정",
      "optionType": "SINGLE",
      "discountAvailable": false,
      "originalPrice": 100000,
      "discountPrice": 80000,
      "description": "수정된 옵션 설명",
      "partnerShops": [
        {
          "category": "HAIR_MAKEUP",
          "name": "샘플샵",
          "link": "http://example.com"
        }
      ]
      // 기타 옵션 관련 정보...
    }
    // 나머지 옵션들...
  ]
}
```

- **`mainImage` / `subImages` / `additionalImages`**: 각 이미지의 `imageId`와 `url`이 함께 제공됩니다.
- **`options`**: 수정된(또는 추가된) 옵션 정보가 포함되어 있으며, 각각의 파트너샵 정보도 응답에 포함됩니다.

---

이상으로 상품 업데이트 API 문서를 마칩니다.
