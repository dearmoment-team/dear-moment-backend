package kr.kro.dearmoment.product.adapter.input.web.get

import andDocument
import kr.kro.dearmoment.common.dto.ResponseWrapper
import kr.kro.dearmoment.common.restdocs.ARRAY
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.product.adapter.input.web.ProductRestAdapter
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.usecase.create.CreateProductUseCase
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductUseCase
import kr.kro.dearmoment.product.application.usecase.get.GetProductUseCase
import kr.kro.dearmoment.product.application.usecase.search.ProductSearchUseCase
import kr.kro.dearmoment.product.application.usecase.update.UpdateProductUseCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(RestDocumentationExtension::class)
@WebMvcTest(ProductRestAdapter::class)
@Import(ResponseWrapper::class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
class GetProductRestAdapterTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var updateProductUseCase: UpdateProductUseCase

    @MockitoBean
    lateinit var createProductUseCase: CreateProductUseCase

    @MockitoBean
    lateinit var deleteProductUseCase: DeleteProductUseCase

    @MockitoBean
    lateinit var getProductUseCase: GetProductUseCase

    @MockitoBean
    lateinit var productSearchUseCase: ProductSearchUseCase

    @Test
    fun `상품 단건 조회 API 테스트 - 정상 조회`() {
        // given
        val productResponse =
            ProductResponse(
                productId = 1L,
                userId = 1L,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "New Product",
                description = "Product description",
                availableSeasons = listOf("YEAR_2025_FIRST_HALF"),
                cameraTypes = listOf("DIGITAL"),
                retouchStyles = listOf("MODERN"),
                mainImage = "http://image-server.com/mainImage.jpg",
                subImages =
                    listOf(
                        "http://image-server.com/subImage1.jpg",
                        "http://image-server.com/subImage2.jpg",
                        "http://image-server.com/subImage3.jpg",
                        "http://image-server.com/subImage4.jpg",
                    ),
                additionalImages = emptyList(),
                detailedInfo = "Detailed product information",
                contactInfo = "contact@example.com",
                createdAt = null,
                updatedAt = null,
                options = emptyList(),
            )
        given(getProductUseCase.getProductById(1L)).willReturn(productResponse)

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
                    "data.userId" type NUMBER means "사용자 ID",
                    "data.productType" type STRING means "상품 유형",
                    "data.shootingPlace" type STRING means "촬영 장소",
                    "data.title" type STRING means "상품명",
                    "data.description" type STRING means "상품 설명",
                    "data.availableSeasons" type ARRAY means "촬영 가능 시기 목록",
                    "data.cameraTypes" type ARRAY means "카메라 종류 목록",
                    "data.retouchStyles" type ARRAY means "보정 스타일 목록",
                    "data.mainImage" type STRING means "대표 이미지 URL",
                    "data.subImages" type ARRAY means "서브 이미지 URL",
                    "data.additionalImages" type ARRAY means "추가 이미지 URL",
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
        given(getProductUseCase.getProductById(999L))
            .willThrow(IllegalArgumentException("Product with ID 999 not found."))

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
