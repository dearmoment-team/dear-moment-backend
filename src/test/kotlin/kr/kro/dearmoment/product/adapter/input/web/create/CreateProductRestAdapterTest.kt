package kr.kro.dearmoment.product.adapter.input.web.create

import andDocument
import io.mockk.every
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.restdocs.ARRAY
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.toJsonString
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.product.application.dto.response.ImageResponse
import kr.kro.dearmoment.product.application.dto.response.PartnerShopResponse
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class CreateProductRestAdapterTest : RestApiTestBase() {
    @Test
    fun `상품 생성 API 테스트`() {
        // main image 파일
        val mainImage = MockMultipartFile(
            "mainImageFile",
            "mainImage.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "main image content".toByteArray()
        )

        // sub image 파일 4장
        val subImage1 = MockMultipartFile(
            "subImageFiles",
            "subImage1.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "sub image 1".toByteArray()
        )
        val subImage2 = MockMultipartFile(
            "subImageFiles",
            "subImage2.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "sub image 2".toByteArray()
        )
        val subImage3 = MockMultipartFile(
            "subImageFiles",
            "subImage3.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "sub image 3".toByteArray()
        )
        val subImage4 = MockMultipartFile(
            "subImageFiles",
            "subImage4.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "sub image 4".toByteArray()
        )

        // CreateProductRequest JSON 생성 (userId 필드는 제거)
        val requestMap = mapOf(
            "productType" to "WEDDING_SNAP",
            "shootingPlace" to "JEJU",
            "title" to "New Product",
            "description" to "Product description",
            "availableSeasons" to listOf("YEAR_2025_FIRST_HALF"),
            "cameraTypes" to listOf("DIGITAL"),
            "retouchStyles" to listOf("MODERN"),
            "detailedInfo" to "Detailed product information",
            "contactInfo" to "contact@example.com",
            "options" to listOf(
                mapOf(
                    "name" to "Option 1",
                    "optionType" to "SINGLE",
                    "discountAvailable" to false,
                    "originalPrice" to 10000,
                    "discountPrice" to 1000,
                    "description" to "Option description",
                    "costumeCount" to 1,
                    "shootingLocationCount" to 1,
                    "shootingHours" to 1,
                    "shootingMinutes" to 30,
                    "retouchedCount" to 1,
                    "partnerShops" to listOf(
                        mapOf("category" to "DRESS", "name" to "Shop A", "link" to "http://shopA.com"),
                        mapOf("category" to "DRESS", "name" to "Shop B", "link" to "http://shopB.com")
                    )
                ),
                mapOf(
                    "name" to "Option 2",
                    "optionType" to "PACKAGE",
                    "discountAvailable" to true,
                    "originalPrice" to 20000,
                    "discountPrice" to 15000,
                    "description" to "Package option",
                    "costumeCount" to 0,
                    "shootingLocationCount" to 0,
                    "shootingHours" to 0,
                    "shootingMinutes" to 0,
                    "retouchedCount" to 0,
                    "partnerShops" to listOf(
                        mapOf("category" to "DRESS", "name" to "Shop C", "link" to "http://shopC.com")
                    )
                )
            )
        )
        val jsonRequest = requestMap.toJsonString()
        val requestPart = MockMultipartFile(
            "request",
            "request.json",
            MediaType.APPLICATION_JSON_VALUE,
            jsonRequest.toByteArray()
        )

        // 응답 객체 설정 (userId는 UUID 타입, 예: "550e8400-e29b-41d4-a716-446655440000")
        val dummyUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val productOptionResponse1 = ProductOptionResponse(
            optionId = 1L,
            productId = 1L,
            name = "Option 1",
            optionType = "SINGLE",
            discountAvailable = false,
            originalPrice = 10000,
            discountPrice = 1000,
            description = "Option description",
            costumeCount = 1,
            shootingLocationCount = 1,
            shootingHours = 1,
            shootingMinutes = 30,
            retouchedCount = 1,
            partnerShops = listOf(
                PartnerShopResponse("DRESS", "Shop A", "http://shopA.com"),
                PartnerShopResponse("DRESS", "Shop B", "http://shopB.com")
            ),
            createdAt = null,
            updatedAt = null
        )
        val productOptionResponse2 = ProductOptionResponse(
            optionId = 2L,
            productId = 1L,
            name = "Option 2",
            optionType = "PACKAGE",
            discountAvailable = true,
            originalPrice = 20000,
            discountPrice = 15000,
            description = "Package option",
            costumeCount = 0,
            shootingLocationCount = 0,
            shootingHours = 0,
            shootingMinutes = 0,
            retouchedCount = 0,
            partnerShops = listOf(
                PartnerShopResponse("DRESS", "Shop C", "http://shopC.com")
            ),
            createdAt = null,
            updatedAt = null
        )
        val productResponse = ProductResponse(
            productId = 1L,
            userId = dummyUserId, // UUID 타입
            productType = "WEDDING_SNAP",
            shootingPlace = "JEJU",
            title = "New Product",
            description = "Product description",
            availableSeasons = listOf("YEAR_2025_FIRST_HALF"),
            cameraTypes = listOf("DIGITAL"),
            retouchStyles = listOf("MODERN"),
            mainImage = ImageResponse(imageId = 1L, url = "http://image-server.com/mainImage.jpg"),
            subImages = listOf(
                ImageResponse(imageId = 2L, url = "http://image-server.com/subImage1.jpg"),
                ImageResponse(imageId = 3L, url = "http://image-server.com/subImage2.jpg"),
                ImageResponse(imageId = 4L, url = "http://image-server.com/subImage3.jpg"),
                ImageResponse(imageId = 5L, url = "http://image-server.com/subImage4.jpg")
            ),
            additionalImages = emptyList(),
            detailedInfo = "Detailed product information",
            contactInfo = "contact@example.com",
            createdAt = null,
            updatedAt = null,
            options = listOf(productOptionResponse1, productOptionResponse2)
        )

        // createProductUseCase 모의 동작 설정
        // 파라미터: (CreateProductRequest, userId: UUID, mainImageFile, subImageFiles, additionalImageFiles)
        every { createProductUseCase.saveProduct(any(), any(), any(), any(), any()) } returns productResponse

        // multipart 요청 빌드 (인증된 userId는 컨트롤러에서 @AuthenticationPrincipal으로 주입)
        val requestBuilder = multipart("/api/products")
            .file(requestPart)
            .file(mainImage)
            .file(subImage1)
            .file(subImage2)
            .file(subImage3)
            .file(subImage4)
            .characterEncoding("UTF-8")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .accept(MediaType.APPLICATION_JSON)

        // 요청 실행 및 검증
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "create-product",
                responseBody(
                    "success" type BOOLEAN means "요청 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "실제 상품 데이터",
                    "data.productId" type NUMBER means "상품 ID",
                    "data.userId" type STRING means "사용자 ID", // UUID 타입은 문자열로 표현
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
                    "data.createdAt" type OBJECT means "생성 시간",
                    "data.updatedAt" type OBJECT means "수정 시간",
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
                    "data.options[].partnerShops[].category" type STRING means "제휴 업체 카테고리",
                    "data.options[].partnerShops[].name" type STRING means "제휴 업체 이름",
                    "data.options[].partnerShops[].link" type STRING means "제휴 업체 링크",
                    "data.options[].createdAt" type OBJECT means "옵션 생성 시간",
                    "data.options[].updatedAt" type OBJECT means "옵션 수정 시간",
                )
            )
    }
}
