package kr.kro.dearmoment.product.adapter.input.web.get

import andDocument
import io.mockk.every
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.restdocs.ARRAY
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.product.application.dto.response.ImageResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class GetProductRestAdapterTest : RestApiTestBase() {
    @Test
    fun `상품 단건 조회 API 테스트 - 정상 조회`() {
        // given
        val dummyUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val productResponse =
            ProductResponse(
                productId = 1L,
                userId = dummyUserId,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "New Product",
                description = "Product description",
                availableSeasons = listOf("YEAR_2025_FIRST_HALF"),
                cameraTypes = listOf("DIGITAL"),
                retouchStyles = listOf("MODERN"),
                mainImage = ImageResponse(imageId = 1L, url = "http://image-server.com/mainImage.jpg"),
                subImages =
                    listOf(
                        ImageResponse(imageId = 2L, url = "http://image-server.com/subImage1.jpg"),
                        ImageResponse(imageId = 3L, url = "http://image-server.com/subImage2.jpg"),
                        ImageResponse(imageId = 4L, url = "http://image-server.com/subImage3.jpg"),
                        ImageResponse(imageId = 5L, url = "http://image-server.com/subImage4.jpg"),
                    ),
                additionalImages = emptyList(),
                detailedInfo = "Detailed product information",
                contactInfo = "contact@example.com",
                createdAt = null,
                updatedAt = null,
                options = emptyList(),
            )
        every { getProductUseCase.getProductById(1L) } returns productResponse

        // when
        val requestBuilder =
            RestDocumentationRequestBuilders
                .get("/api/products/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "get-product",
                responseBody(
                    "success" type BOOLEAN means "요청 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "상품 데이터",
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
                    "data.createdAt" type OBJECT means "생성 시간",
                    "data.updatedAt" type OBJECT means "수정 시간",
                    "data.options" type ARRAY means "옵션 목록",
                ),
            )
    }

    @Test
    fun `상품 단건 조회 API 테스트 - 존재하지 않는 상품`() {
        // given
        every { getProductUseCase.getProductById(999L) } throws
            IllegalArgumentException("Product with ID 999 not found.")

        // when
        val requestBuilder =
            RestDocumentationRequestBuilders
                .get("/api/products/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument(
                "get-product-not-found",
                responseBody(
                    "message" type STRING means "에러 메시지",
                ),
            )
    }
}
