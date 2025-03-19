# 상품 업데이트 API 문서

이 문서는 상품 업데이트 API에 대한 상세한 설명을 담고 있습니다.  
상품 업데이트는 크게 **상품 정보 업데이트**와 **옵션 업데이트** 두 부분으로 구성됩니다.  
이미지 파일(메인, 서브, 추가)은 별도의 Multipart로 전송되며, JSON 본문에는 이미지 처리에 관한 지침(액션, 인덱스, 이미지ID)만 포함됩니다.

---

## 1. 상품 정보 업데이트

### 1.1 개요
- **목적**: 기존 상품의 기본 정보를 업데이트합니다.
- **HTTP 메서드**: PATCH
- **엔드포인트**: `/api/products/{id}`
- **Content-Type**: `multipart/form-data`

### 1.2 요청 JSON 구조

업데이트 요청 JSON은 아래와 같은 필드들로 구성됩니다.

| 필드명                  | 타입             | 필수 여부 | 설명 |
|-------------------------|------------------|-----------|------|
| `productId`             | number           | 필수      | 업데이트할 상품의 ID |
| `userId`                | number           | 필수      | 상품 소유자의 사용자 ID |
| `productType`           | string           | 선택      | 상품 유형 (예: `"WEDDING_SNAP"`) (미전달 시 기존 값 유지) |
| `shootingPlace`         | string           | 선택      | 촬영 장소 (예: `"JEJU"`) (미전달 시 기존 값 유지) |
| `title`                 | string           | 선택      | 상품 제목 (미전달 시 기존 값 유지) |
| `description`           | string           | 선택      | 상품 설명 (미전달 시 기존 값 유지) |
| `detailedInfo`          | string           | 선택      | 상세 정보 (연락처, 문의방법 등) |
| `availableSeasons`      | array(string)    | 선택      | 촬영 가능 시기 (예: `["YEAR_2025_FIRST_HALF", "YEAR_2025_SECOND_HALF"]`) |
| `cameraTypes`           | array(string)    | 선택      | 카메라 종류 (예: `["DIGITAL", "FILM"]`) |
| `retouchStyles`         | array(string)    | 선택      | 보정 스타일 (예: `["MODERN", "VINTAGE"]`) |
| `contactInfo`           | string           | 선택      | 연락처 정보 |
| `subImagesFinal`        | array(object)    | 선택      | **서브 이미지** 처리 지침 (아래 참조) |
| `additionalImagesFinal` | array(object)    | 선택      | **추가 이미지** 처리 지침 (아래 참조) |

#### 1.2.1 서브 이미지 처리 지침 (subImagesFinal)
각 객체는 다음 필드를 포함합니다:
- **`action`**: `"KEEP"`, `"DELETE"`, `"UPLOAD"`
    - **KEEP**: 기존 이미지를 그대로 유지
    - **DELETE**: 기존 이미지를 삭제 (단독 사용 불가; 반드시 같은 인덱스에서 UPLOAD와 함께 사용)
    - **UPLOAD**: 새 이미지를 업로드 (기존 이미지를 대체)
- **`index`**: 서브 이미지 배열에서 해당 이미지의 위치 (0부터 시작, 반드시 0~3)
- **`imageId`**: 기존 이미지의 ID (KEEP, DELETE 시 필요하며, UPLOAD 시에는 `null`)

#### 1.2.2 추가 이미지 처리 지침 (additionalImagesFinal)
각 객체는 다음 필드를 포함합니다:
- **`action`**: `"KEEP"`, `"DELETE"`, `"UPLOAD"`
    - **KEEP**: 기존 이미지를 그대로 유지
    - **DELETE**: 기존 이미지를 삭제
    - **UPLOAD**: 새 이미지를 업로드
- **`imageId`**: 기존 이미지의 ID (KEEP, DELETE 시 필요, UPLOAD 시에는 `null`)

> **참고**: 서브 이미지는 **항상 4장**을 유지해야 하며, 추가 이미지는 **최대 5장**을 넘지 않아야 합니다.

### 1.3 예시 요청 JSON (상품 정보 업데이트)

아래 예시는 가장 복잡한 시나리오를 가정한 것으로,
- **메인 이미지**는 새 파일로 교체
- **서브 이미지** 4장 중 인덱스 1과 3은 교체(각각 DELETE + UPLOAD), 인덱스 0와 2는 그대로 유지
- **추가 이미지**는 기존 5장 중 일부를 삭제하고, 2장의 새 파일을 업로드하여 최종 결과가 5장 이하가 되도록 함

```json
{
  "productId": 1,
  "userId": 1,
  "productType": "WEDDING_SNAP",
  "shootingPlace": "JEJU",
  "title": "예쁜 웨딩 사진 촬영(업데이트)",
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
    { "action": "KEEP", "index": 0, "imageId": 2 },
    { "action": "DELETE", "index": 1, "imageId": 3 },
    { "action": "UPLOAD", "index": 1, "imageId": null },
    { "action": "KEEP", "index": 2, "imageId": 4 },
    { "action": "DELETE", "index": 3, "imageId": 5 },
    { "action": "UPLOAD", "index": 3, "imageId": null }
  ],
  "additionalImagesFinal": [
    { "action": "KEEP", "imageId": 6 },
    { "action": "DELETE", "imageId": 7 },
    { "action": "KEEP", "imageId": 8 },
    { "action": "DELETE", "imageId": 9 },
    { "action": "KEEP", "imageId": 10 },
    { "action": "UPLOAD", "imageId": null },
    { "action": "UPLOAD", "imageId": null }
  ]
}
```

## 2. 옵션 업데이트

### 2.1 개요
옵션 업데이트는 기존 옵션 수정과 새 옵션 추가를 포함합니다.
각 옵션 객체는 아래 필드를 포함합니다.

| 필드명 | 타입 | 설명 |
|--------|------|------|
| optionId | number 또는 null | 기존 옵션 수정 시 ID, 새 옵션 추가 시 null |
| name | string | 옵션명 |
| optionType | string | 옵션 유형 ("SINGLE", "PACKAGE") |
| discountAvailable | boolean | 할인 적용 여부 |
| originalPrice | number | 원래 가격 |
| discountPrice | number | 할인 가격 |
| description | string | 옵션 설명 |
| costumeCount | number | 의상 수량 (단품 옵션의 경우 1 이상) |
| shootingLocationCount | number | 촬영 장소 수 (단품 옵션의 경우 1 이상) |
| shootingHours | number | 촬영 시간 (시) |
| shootingMinutes | number | 촬영 시간 (분) |
| retouchedCount | number | 보정된 사진 수 (단품 옵션의 경우 1 이상) |
| originalProvided | boolean | 원본 제공 여부 |
| partnerShops | array(object) | 파트너샵 목록 (각 객체는 category, name, link 포함) |

### 2.2 예시 옵션 JSON
아래 예시는 기존 옵션 1개를 수정하고, 새 옵션 2개를 추가하는 경우입니다.

```json
[
  {
    "optionId": 1,
    "name": "옵션1 (수정)",
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
        "name": "메이크업샵",
        "link": "http://example.com"
      }
    ]
  },
  {
    "optionId": null,
    "name": "새로운 옵션 추가",
    "optionType": "PACKAGE",
    "discountAvailable": true,
    "originalPrice": 200000,
    "discountPrice": 150000,
    "description": "새로 추가된 패키지 옵션",
    "costumeCount": 2,
    "shootingLocationCount": 2,
    "shootingHours": 3,
    "shootingMinutes": 45,
    "retouchedCount": 2,
    "originalProvided": false,
    "partnerShops": [
      {
        "category": "DRESS",
        "name": "드레스샵",
        "link": "http://dress.example"
      }
    ]
  },
  {
    "optionId": null,
    "name": "또 다른 새 옵션",
    "optionType": "SINGLE",
    "discountAvailable": false,
    "originalPrice": 80000,
    "discountPrice": 60000,
    "description": "새로 추가된 단품 옵션",
    "costumeCount": 1,
    "shootingLocationCount": 1,
    "shootingHours": 1,
    "shootingMinutes": 15,
    "retouchedCount": 1,
    "originalProvided": true,
    "partnerShops": [
      {
        "category": "VIDEO",
        "name": "비디오샵",
        "link": "http://video.example"
      }
    ]
  }
]
```

## 3. 파일 업로드 구성 (Multipart)

### (1) 메인 이미지 파일
- **mainImageFile**: 대표 이미지 교체용 → 1개 파일 첨부

### (2) 서브 이미지 파일
- **subImageFiles**:
    - 위 JSON의 subImagesFinal에서 UPLOAD 액션이 적용된 인덱스는 index 1와 index 3 → 2개 파일 첨부
    - 각 파일은 해당 인덱스의 새 이미지를 업로드하는 데 사용됩니다.

### (3) 추가 이미지 파일
- **additionalImageFiles**:
    - 위 JSON의 additionalImagesFinal에서 UPLOAD 액션은 2개 → 2개 파일 첨부

### 총 파일 첨부 개수:
- 메인 이미지: 1개
- 서브 이미지: 2개
- 추가 이미지: 2개

## 4. React 상태관리 조언

### 서브 이미지 (4장 고정)

상태 예시:
```javascript
const [subImages, setSubImages] = useState([
  { id: 2, action: "KEEP" },
  { id: 3, action: "KEEP" },
  { id: 4, action: "KEEP" },
  { id: 5, action: "KEEP" }
]);
```

사용자가 특정 인덱스(예: index 1)의 이미지를 교체할 경우,
해당 인덱스에 대해 action을 "DELETE"와 함께 "UPLOAD"를 지정합니다.
최종적으로 상태 배열은 항상 4개의 항목(서브 이미지)을 유지하며,
이를 기반으로 JSON의 subImagesFinal 배열을 생성하여 서버에 전송합니다.

### 추가 이미지 (최대 5장)

상태 예시:
```javascript
const [additionalImages, setAdditionalImages] = useState([
  { id: 6, action: "KEEP" },
  { id: 7, action: "KEEP" },
  { id: 8, action: "KEEP" },
  { id: 9, action: "KEEP" },
  { id: 10, action: "KEEP" }
]);
```

사용자가 추가 이미지를 삭제하면, 해당 항목의 action을 "DELETE"로 변경합니다.
새 이미지를 추가할 때는, 새로운 항목 { id: null, action: "UPLOAD", file: ... }를 추가하되,
최종 배열의 길이가 5장을 넘지 않도록 유효성 검사를 실시합니다.

상태관리를 통해 UI에서 각 이미지의 상태(KEEP, DELETE, UPLOAD)를 시각적으로 표시하고,
최종 Submit 시 올바른 JSON과 파일 배열을 전송하도록 합니다.

## 5. 응답 예시

성공적으로 업데이트되면 서버는 다음과 같은 응답을 반환합니다.

```json
{
  "productId": 1,
  "userId": 1,
  "productType": "WEDDING_SNAP",
  "shootingPlace": "JEJU",
  "title": "예쁜 웨딩 사진 촬영(업데이트)",
  "description": "신랑, 신부의 아름다운 순간을 담은 웨딩 사진",
  "availableSeasons": ["YEAR_2025_FIRST_HALF", "YEAR_2025_SECOND_HALF"],
  "cameraTypes": ["DIGITAL", "FILM"],
  "retouchStyles": ["MODERN", "VINTAGE"],
  "mainImage": {
    "imageId": 9999,
    "url": "https://objectstorage.ap-chuncheon-1.oci.customer-oci.com/.../main.jpg"
  },
  "subImages": [
    {
      "imageId": 10000,
      "url": "https://objectstorage.ap-chuncheon-1.oci.customer-oci.com/.../subImage1.jpg"
    },
    {
      "imageId": 10001,
      "url": "https://objectstorage.ap-chuncheon-1.oci.customer-oci.com/.../subImage2.jpg"
    },
    {
      "imageId": 4,
      "url": "https://objectstorage.ap-chuncheon-1.oci.customer-oci.com/.../subImage3.jpg"
    },
    {
      "imageId": 5,
      "url": "https://objectstorage.ap-chuncheon-1.oci.customer-oci.com/.../subImage4.jpg"
    }
  ],
  "additionalImages": [
    {
      "imageId": 20000,
      "url": "https://objectstorage.ap-chuncheon-1.oci.customer-oci.com/.../add1.jpg"
    },
    {
      "imageId": 20001,
      "url": "https://objectstorage.ap-chuncheon-1.oci.customer-oci.com/.../add2.jpg"
    }
  ],
  "detailedInfo": "연락처: 010-1234-5678, 상세 문의는 이메일로",
  "contactInfo": "010-1234-5678",
  "createdAt": "2025-03-17T12:00:00",
  "updatedAt": "2025-03-17T12:30:00",
  "options": [
    {
      "optionId": 1,
      "productId": 1,
      "name": "옵션1 (수정)",
      "optionType": "SINGLE",
      "discountAvailable": false,
      "originalPrice": 100000,
      "discountPrice": 80000,
      "description": "수정된 옵션 설명",
      "partnerShops": [
        {
          "category": "HAIR_MAKEUP",
          "name": "메이크업샵",
          "link": "http://example.com"
        }
      ]
    },
    {
      "optionId": 101,
      "productId": 1,
      "name": "새로운 옵션 추가",
      "optionType": "PACKAGE",
      "discountAvailable": true,
      "originalPrice": 200000,
      "discountPrice": 150000,
      "description": "새로 추가된 패키지 옵션",
      "partnerShops": [
        {
          "category": "DRESS",
          "name": "드레스샵",
          "link": "http://dress.example"
        }
      ]
    }
  ]
}
```

## 결론

- 상품 업데이트 API는 상품 기본 정보, 이미지(메인, 서브, 추가), 옵션을 한 번에 업데이트합니다.
- 이미지 파일은 별도의 Multipart 파트로 전송되며, JSON 본문에는 이미지 처리 지침(KEEP, DELETE, UPLOAD)만 포함합니다.
- 서브 이미지는 항상 4장을 유지해야 하므로, "DELETE" 액션은 반드시 같은 인덱스에서 "UPLOAD"와 함께 사용되어야 합니다.
- 추가 이미지는 최종 결과가 5장을 초과하면 안 됩니다.
- React 측에서는 각 이미지 배열(서브, 추가)을 상태로 관리하여, 사용자 액션에 따라 "KEEP", "DELETE", "UPLOAD"를 업데이트한 후 최종적으로 JSON과 파일들을 함께 제출하면 됩니다.
- 상태 관리를 통해 사용자 인터페이스에서 현재 이미지의 상태와 미리보기를 제공하고, 잘못된 입력을 방지하는 로직을 구현하는 것이 중요합니다.
- 이 문서를 참고하여, 상품 업데이트 요청을 구성하면 서버에서 올바르게 업데이트를 수행할 수 있습니다.
