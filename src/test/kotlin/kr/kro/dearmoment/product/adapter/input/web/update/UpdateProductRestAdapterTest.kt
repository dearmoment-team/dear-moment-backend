package kr.kro.dearmoment.product.adapter.input.web.update

import andDocument
import io.mockk.every
import io.mockk.mockk
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.restdocs.ARRAY
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.product.application.dto.response.ImageResponse
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.usecase.update.UpdateProductUseCase
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

/**
 * [상품 업데이트] Controller 테스트 예시 (PATCH 방식)
 */
class UpdateProductRestAdapterTest : RestApiTestBase() {
    @Test
    fun `상품 업데이트 API 테스트 - 정상 케이스`() {
        // 1) 업데이트 요청 DTO(JSON) - DTO에는 userId가 없고, studioId가 포함됩니다.
        val requestJson =
            """
            {
              "productId": 1,
              "studioId": 100,
              "productType": "WEDDING_SNAP",
              "shootingPlace": "JEJU",
              "title": "Updated Product Title",
              "description": "Updated Description",
              "availableSeasons": ["YEAR_2025_SECOND_HALF"],
              "cameraTypes": ["DIGITAL"],
              "retouchStyles": ["CALM"],
              "subImagesFinal": [
                { "action": "KEEP", "index": 0, "imageId": 200 },
                { "action": "DELETE", "index": 1, "imageId": 201 },
                { "action": "UPLOAD", "index": 2, "imageId": null },
                { "action": "UPLOAD", "index": 3, "imageId": null }
              ],
              "additionalImagesFinal": [
                { "action": "DELETE", "imageId": 300 },
                { "action": "UPLOAD", "imageId": null }
              ],
              "detailedInfo": "Updated Detailed Info",
              "contactInfo": "updated-contact@example.com",
              "options": [
                {
                  "optionId": 1,
                  "name": "New Option 1",
                  "optionType": "SINGLE",
                  "discountAvailable": true,
                  "originalPrice": 10000,
                  "discountPrice": 7000,
                  "description": "Updated Option Desc",
                  "costumeCount": 1,
                  "shootingLocationCount": 1,
                  "shootingHours": 2,
                  "shootingMinutes": 0,
                  "retouchedCount": 3,
                  "originalProvided": true,
                  "partnerShops": []
                }
              ]
            }
            """.trimIndent()

        // 2) 파일 준비
        val mainImageFile = MockMultipartFile(
            "mainImageFile",
            "updated_main.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "updated main image content".toByteArray()
        )
        val subImageFile1 = MockMultipartFile(
            "subImageFiles",
            "sub3.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "new sub image content #3".toByteArray()
        )
        val subImageFile2 = MockMultipartFile(
            "subImageFiles",
            "sub4.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "new sub image content #4".toByteArray()
        )
        val additionalImageFile = MockMultipartFile(
            "additionalImageFiles",
            "add2.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "new additional image content".toByteArray()
        )

        // 3) 수정 결과(응답) 예시
        val authUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val updatedResponse = ProductResponse(
            productId = 1L,
            userId = authUserId,
            productType = "WEDDING_SNAP",
            shootingPlace = "JEJU",
            title = "Updated Product Title",
            description = "Updated Description",
            availableSeasons = listOf("YEAR_2025_SECOND_HALF"),
            cameraTypes = listOf("DIGITAL"),
            retouchStyles = listOf("CALM"),
            mainImage = ImageResponse(imageId = 101L, url = "http://image-server.com/updated_main.jpg"),
            subImages = listOf(
                ImageResponse(imageId = 102L, url = "http://image-server.com/subImage1_KEPT.jpg"),
                ImageResponse(imageId = 103L, url = "http://image-server.com/subImage3_UPLOADED.jpg"),
                ImageResponse(imageId = 104L, url = "http://image-server.com/subImage4_UPLOADED.jpg")
            ),
            additionalImages = listOf(
                ImageResponse(imageId = 105L, url = "http://image-server.com/additionalImage2_UPLOADED.jpg")
            ),
            detailedInfo = "Updated Detailed Info",
            contactInfo = "updated-contact@example.com",
            createdAt = null,
            updatedAt = null,
            options = listOf(
                ProductOptionResponse(
                    optionId = 1L,
                    productId = 1L,
                    name = "New Option 1",
                    optionType = "SINGLE",
                    discountAvailable = true,
                    originalPrice = 10000,
                    discountPrice = 7000,
                    description = "Updated Option Desc",
                    costumeCount = 1,
                    shootingLocationCount = 1,
                    shootingHours = 2,
                    shootingMinutes = 0,
                    retouchedCount = 3,
                    partnerShops = emptyList(),
                    createdAt = null,
                    updatedAt = null,
                )
            )
        )

        // 4) updateProductUseCase 모킹 (새로운 시그니처에 맞게, 인증된 userId를 첫 번째 파라미터로 전달)
        every {
            updateProductUseCase.updateProduct(eq(authUserId), eq(1L), any(), any(), any(), any(), any())
        } returns updatedResponse

        // 5) multipart/form-data PATCH 요청
        val requestPart = MockMultipartFile(
            "request",
            "request.json",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.toByteArray()
        )
        val requestBuilder = MockMvcRequestBuilders.multipart("/api/products/{id}", 1L)
            .file(requestPart)
            .file(mainImageFile)
            .file(subImageFile1)
            .file(subImageFile2)
            .file(additionalImageFile)
            .with { req ->
                req.method = HttpMethod.PATCH.toString()
                req
            }
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .accept(MediaType.APPLICATION_JSON)

        // 6) 요청 실행 및 검증
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "update-product",
                responseBody(
                    "success" type BOOLEAN means "요청 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "수정된 상품 데이터",
                    "data.productId" type NUMBER means "상품 ID",
                    "data.userId" type STRING means "사용자 ID",
                    "data.productType" type STRING means "상품 유형",
                    "data.shootingPlace" type STRING means "촬영 장소",
                    "data.title" type STRING means "상품명",
                    "data.description" type STRING means "상품 설명",
                    "data.availableSeasons" type ARRAY means "촬영 가능 시기 목록",
                    "data.cameraTypes" type ARRAY means "카메라 종류 목록",
                    "data.retouchStyles" type ARRAY means "보정 스타일 목록",
                    "data.mainImage" type OBJECT means "대표 이미지",
                    "data.mainImage.imageId" type NUMBER means "대표 이미지 ID",
                    "data.mainImage.url" type STRING means "대표 이미지 URL",
                    "data.subImages" type ARRAY means "서브 이미지 목록",
                    "data.subImages[].imageId" type NUMBER means "서브 이미지 ID",
                    "data.subImages[].url" type STRING means "서브 이미지 URL",
                    "data.additionalImages" type ARRAY means "추가 이미지 목록",
                    "data.additionalImages[].imageId" type NUMBER means "추가 이미지 ID",
                    "data.additionalImages[].url" type STRING means "추가 이미지 URL",
                    "data.detailedInfo" type STRING means "상세 정보",
                    "data.contactInfo" type STRING means "연락처",
                    "data.createdAt" type OBJECT means "생성 일자",
                    "data.updatedAt" type OBJECT means "수정 일자",
                    "data.options" type ARRAY means "옵션 목록",
                    "data.options[].optionId" type NUMBER means "옵션 ID",
                    "data.options[].productId" type NUMBER means "상품 ID",
                    "data.options[].name" type STRING means "옵션명",
                    "data.options[].optionType" type STRING means "옵션 타입",
                    "data.options[].discountAvailable" type BOOLEAN means "할인 가능 여부",
                    "data.options[].originalPrice" type NUMBER means "원 판매가",
                    "data.options[].discountPrice" type NUMBER means "할인가",
                    "data.options[].description" type STRING means "옵션 설명",
                    "data.options[].costumeCount" type NUMBER means "의상 수",
                    "data.options[].shootingLocationCount" type NUMBER means "촬영 장소 수",
                    "data.options[].shootingHours" type NUMBER means "촬영 시간(시)",
                    "data.options[].shootingMinutes" type NUMBER means "촬영 시간(분)",
                    "data.options[].retouchedCount" type NUMBER means "보정본 수",
                    "data.options[].partnerShops" type ARRAY means "제휴 업체 목록",
                    "data.options[].createdAt" type OBJECT means "옵션 생성 시간",
                    "data.options[].updatedAt" type OBJECT means "옵션 수정 시간",
                )
            )
    }
}
