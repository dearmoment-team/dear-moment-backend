# 상품 업데이트 API 문서

이 문서는 상품 업데이트 API에 대한 상세한 설명과, 실제 Swagger에서 multipart/form-data 형태로 전송할 때 겪을 수 있는 문제와 해결 방법을 정리합니다.

상품 업데이트는 크게 **상품 정보 업데이트**와 **옵션 업데이트** 두 부분으로 구성됩니다.
이미지 파일(메인, 서브, 추가)은 별도의 Multipart로 전송되며, JSON 본문에는 이미지 처리에 관한 지침(action, index, imageId)만 포함됩니다.

## 목차
1. [상품 정보 업데이트](#1-상품-정보-업데이트)
2. [옵션 업데이트](#2-옵션-업데이트)
3. [파일 업로드 구성 (Multipart)](#3-파일-업로드-구성-multipart)
4. [React 상태관리 조언](#4-react-상태관리-조언)
5. [응답 예시](#5-응답-예시)
6. [API 사용 시 자주 겪는 문제 & 주의사항](#6-api-사용-시-자주-겪는-문제--주의사항)
7. [결론](#7-결론)

## 1. 상품 정보 업데이트

### 1.1 개요
- **목적**: 기존 상품의 기본 정보를 업데이트합니다.
- **HTTP 메서드**: PATCH
- **엔드포인트**: `/api/products/{id}`
- **Content-Type**: `multipart/form-data`

### 1.2 요청 JSON 구조
업데이트 요청 JSON은 아래와 같은 필드들로 구성됩니다.

| 필드명 | 타입 | 필수 여부 | 설명 |
|--------|------|-----------|------|
| productId | number | 필수 | 업데이트할 상품의 ID |
| userId | number | 필수 | 상품 소유자의 사용자 ID |
| productType | string | 선택 | 상품 유형 (예: "WEDDING_SNAP") (미전달 시 기존 값 유지) |
| shootingPlace | string | 선택 | 촬영 장소 (예: "JEJU") (미전달 시 기존 값 유지) |
| title | string | 선택 | 상품 제목 (미전달 시 기존 값 유지) |
| description | string | 선택 | 상품 설명 (미전달 시 기존 값 유지) |
| detailedInfo | string | 선택 | 상세 정보 (연락처, 문의방법 등) |
| availableSeasons | array(string) | 선택 | 촬영 가능 시기 (예: ["YEAR_2025_FIRST_HALF", "YEAR_2025_SECOND_HALF"]) |
| cameraTypes | array(string) | 선택 | 카메라 종류 (예: ["DIGITAL", "FILM"]) |
| retouchStyles | array(string) | 선택 | 보정 스타일 (예: ["MODERN", "VINTAGE"]) |
| contactInfo | string | 선택 | 연락처 정보 |
| subImagesFinal | array(object) | 선택 | 서브 이미지 처리 지침 (아래 참조) |
| additionalImagesFinal | array(object) | 선택 | 추가 이미지 처리 지침 (아래 참조) |

#### 1.2.1 서브 이미지 처리 지침 (subImagesFinal)
각 객체는 다음 필드를 포함합니다:

- **action**: "KEEP", "DELETE", "UPLOAD"
  - **KEEP**: 기존 이미지를 그대로 유지
  - **DELETE**: 기존 이미지를 삭제 (단독 사용 불가; 반드시 같은 인덱스에서 UPLOAD와 함께 사용)
  - **UPLOAD**: 새 이미지를 업로드 (기존 이미지를 대체)
- **index**: 서브 이미지 배열에서 해당 이미지의 위치 (0부터 시작, 반드시 0~3)
- **imageId**: 기존 이미지의 ID (KEEP, DELETE 시 필요, UPLOAD 시에는 null)

**주의**: 서브 이미지는 항상 4장을 유지해야 합니다.

기존 이미지를 없애고 새로 등록하려면, 동일 인덱스에서 "DELETE" + "UPLOAD"를 동시에 사용해야 합니다.

#### 1.2.2 추가 이미지 처리 지침 (additionalImagesFinal)
각 객체는 다음 필드를 포함합니다:

- **action**: "KEEP", "DELETE", "UPLOAD"
  - **KEEP**: 기존 이미지를 그대로 유지
  - **DELETE**: 기존 이미지를 삭제
  - **UPLOAD**: 새 이미지를 업로드
- **imageId**: 기존 이미지의 ID (KEEP, DELETE 시 필요, UPLOAD 시에는 null)

**참고**: 추가 이미지는 최대 5장을 넘지 않아야 합니다.

### 1.3 예시 요청 JSON (상품 정보 업데이트)
아래 예시는 가장 복잡한 시나리오를 가정한 것으로,
- 메인 이미지는 새 파일로 교체
- 서브 이미지 4장 중 인덱스 1과 3을 교체 (각각 DELETE + UPLOAD), 인덱스 0와 2는 그대로 유지
- 추가 이미지는 기존 5장 중 일부 삭제 + 2장 새 업로드 (최종 5장 이하)

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

| 필드명 | 타입 | 설명 |
|--------|------|------|
| optionId | number \| null | 기존 옵션 수정 시 ID, 새 옵션 추가 시 null |
| name | string | 옵션명 |
| optionType | string | "SINGLE" \| "PACKAGE" (옵션 유형) |
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
| partnerShops | array(object) | 파트너샵 목록 (category, name, link) |

### 2.2 예시 옵션 JSON
다음 예시는 기존 옵션 1개를 수정하고, 새 옵션 2개를 추가하는 시나리오:

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
아래는 예시 JSON에서 "UPLOAD" 액션이 지정된 곳이 몇 개인지에 따라 파일을 첨부하는 방식입니다.

- 메인 이미지 교체: mainImageFile로 1개 파일
- 서브 이미지(subImagesFinal)에서 "UPLOAD" 액션이 몇 번 나왔는지 → 그만큼 subImageFiles에 첨부
- 추가 이미지(additionalImagesFinal)에서 "UPLOAD" 액션이 몇 번 나왔는지 → 그만큼 additionalImageFiles에 첨부

예를 들어,
- 메인 이미지 "UPLOAD": 1개
- 서브 이미지 "UPLOAD" 2개
- 추가 이미지 "UPLOAD" 2개
  → 총 5개의 파일을 각 필드로 분류해서 전송합니다.

## 4. React 상태관리 조언

### 서브 이미지 (4장 고정)

```javascript
const [subImages, setSubImages] = useState([
  { id: 2, action: "KEEP" },
  { id: 3, action: "KEEP" },
  { id: 4, action: "KEEP" },
  { id: 5, action: "KEEP" }
]);
```

- 특정 인덱스 이미지 교체 시: "DELETE" + "UPLOAD"
- 최종 4개가 유지되도록 관리

### 추가 이미지 (최대 5장)

```javascript
const [additionalImages, setAdditionalImages] = useState([
  { id: 6, action: "KEEP" },
  { id: 7, action: "KEEP" },
  { id: 8, action: "KEEP" },
  { id: 9, action: "KEEP" },
  { id: 10, action: "KEEP" }
]);
```

- 새 이미지 추가 시 `{ id: null, action: "UPLOAD" }` 항목 추가
- 최종 5장 이하가 되도록 검사
- 삭제 시 "DELETE"로 변경

## 5. 응답 예시
아래와 같은 구조의 JSON이 성공 시 반환됩니다:

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

## 6. API 사용 시 자주 겪는 문제 & 주의사항
아래는 multipart/form-data와 DTO 매핑 시 자주 발생하는 오류와 해결 방법입니다.

### 6.1 "서브 이미지는 교체만 가능"
서버에서 서브 이미지를 4장 고정으로 요구하는 경우,
"DELETE"만 사용하면 최종 4장이 되지 않아 에러가 납니다.

동일 인덱스에서 "DELETE"와 "UPLOAD"를 함께 전송해야 합니다.

### 6.2 "추가 이미지는 5장 이하로 제한"
새 파일을 업로드하기 전에, 최종 개수가 5장 이하인지 반드시 확인

초과하면 서버가 거부할 수 있습니다.

### 6.3 "Cannot deserialize value of type ... from Object value"
서버 DTO가 배열(List)을 기대하는데, 클라이언트에서 객체({...})를 보낼 때 발생

배열([...])로 보내야 합니다.

예) 서버: `public List<UpdateProductOptionRequest> options;`
클라이언트:

```json
{ 
  "options": {
    "optionId": null, "name": "옵션" ...
  }
}
```
→ 에러

해결:

```json
{
  "options": [
    { "optionId": null, "name": "옵션", ... }
  ]
}
```

### 6.4 "패키지 옵션은 1개 이상의 파트너샵이 필요합니다."
optionType = "PACKAGE"일 경우, 서버 로직에서 파트너샵이 1개 이상인지 검사할 수 있습니다.

- partnerShops 배열이 비어 있으면 에러 발생
- 최소 한 개 이상의 파트너샵 객체 {category, name, link}를 넣어야 합니다.

### 6.5 중간에 request라는 필드가 있는 경우
서버가 최상위 JSON만 파싱(UpdateProductRequest) 하는 구조인데,
클라이언트가 다음처럼 **중간에 request**를 넣으면 매핑 불가

```json
{
  "request": {
    "productId": 1,
    ...
  }
}
```

해결: 중간 객체 없애고 직접 필드를 최상위에 배치

### 6.6 Swagger에서 주의할 점
- JSON 필드는 Swagger UI에서 문자열로 입력
  - 즉, 큰따옴표 안에 JSON을 그대로 넣어야 함 (예: `"{\"productId\":1, ...}"`)
- 파일은 해당 필드(file upload)에 첨부
- subImageFiles, additionalImageFiles 등에서 "UPLOAD"된 개수만큼 파일을 넣습니다.

## 7. 결론

- 상품 기본 정보 업데이트와 옵션 업데이트는 분리된 DTO나 한꺼번에 처리할 수 있음 (서버 구현에 따라 다름).
- 메인/서브/추가 이미지는 JSON에서 "KEEP", "DELETE", "UPLOAD" 지시 후, 실제 파일은 multipart로 첨부.
- 서브 이미지는 4장 고정 → "DELETE"만 쓰면 에러. 반드시 "DELETE"+"UPLOAD" 동시 사용.
- 추가 이미지는 최대 5장 → 초과하면 안 됨.
- 옵션(SINGLE/PACKAGE) 업데이트 시, JSON 배열로 보내고, "PACKAGE"는 최소 1개 파트너샵이 필요할 수 있음.
- Swagger로 테스트할 때는 JSON 직렬화 문제(배열 vs 객체, 중간 필드, etc.)를 특히 주의해야 합니다.

위 내용을 참고하여 API를 호출하면, 올바른 상품 업데이트를 수행할 수 있습니다.