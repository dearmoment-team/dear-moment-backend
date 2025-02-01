package kr.kro.dearmoment.product.adapter.input.web

import andDocument
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.ARRAY
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.BOOLEAN
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.NUMBER
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.OBJECT
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.STRING
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.responseBody
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.toJsonString
import kr.kro.dearmoment.boardgame.adapter.input.web.restdocs.type
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.usecase.ProductUseCase
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(RestDocumentationExtension::class)
@WebMvcTest(ProductRestAdapter::class)
@Import(ProductRestAdapterTestConfig::class, kr.kro.dearmoment.common.advice.GlobalExceptionHandler::class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
class ProductRestAdapterTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var productUseCase: ProductUseCase

    @Test
    fun `상품 생성 API 테스트`() {
        // given
        val request = CreateProductRequest(
            userId = 1L,
            title = "New Product",
            price = 10000,
            typeCode = 0,
            images = listOf("image1.jpg"),
            options = emptyList(),
            contactInfo = "contact@example.com",
            description = "Product description",
            detailedInfo = "Detailed product information",
            numberOfCostumes = 3,
            partnerShops = listOf(),
            shootingLocation = "Location1",
            shootingTime = null,
            warrantyInfo = "blabla",
        )
        val productResponse = ProductResponse(
            productId = 1L,
            userId = 1L,
            title = "New Product",
            description = "Product description",
            price = 10000,
            typeCode = 0,
            shootingTime = null,
            shootingLocation = "Location1",
            numberOfCostumes = 3,
            partnerShops = emptyList(),
            detailedInfo = "Detailed product information",
            warrantyInfo = "blabla",
            contactInfo = "contact@example.com",
            images = listOf("image1.jpg"),
            options = emptyList(),
            createdAt = null,
            updatedAt = null,
        )
        given(productUseCase.saveProduct(request)).willReturn(productResponse)

        val requestBuilder = RestDocumentationRequestBuilders
            .post("/api/products")
            .content(request.toJsonString())
            .contentType(MediaType.APPLICATION_JSON)

        // when/then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isCreated)
            .andDocument(
                "create-product",
                responseBody(
                    // ResponseWrapper에 의해 래핑된 최종 응답 구조
                    "success" type BOOLEAN means "응답 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "상품 데이터",
                    "data.productId" type NUMBER means "상품 ID",
                    "data.userId" type NUMBER means "사용자 ID",
                    "data.title" type STRING means "상품명",
                    "data.description" type STRING means "상품 설명",
                    "data.price" type NUMBER means "가격",
                    "data.typeCode" type NUMBER means "상품 타입 코드",
                    "data.shootingTime" type OBJECT means "촬영 시간",
                    "data.shootingLocation" type STRING means "촬영 장소",
                    "data.numberOfCostumes" type NUMBER means "의상 수",
                    "data.partnerShops" type ARRAY means "협력업체 목록",
                    "data.detailedInfo" type STRING means "상세 정보",
                    "data.warrantyInfo" type STRING means "보증 정보",
                    "data.contactInfo" type STRING means "연락처",
                    "data.createdAt" type OBJECT means "생성 시간",
                    "data.updatedAt" type OBJECT means "수정 시간",
                    "data.options" type ARRAY means "옵션 목록",
                    "data.images" type ARRAY means "상품 이미지 리스트",
                ),
            )
    }

    @Test
    fun `메인페이지 상품 조회 API 테스트`() {
        // given
        val productResponse = ProductResponse(
            productId = 1L,
            userId = 1L,
            title = "New Product",
            description = "Product description",
            price = 10000,
            typeCode = 0,
            shootingTime = null,
            shootingLocation = "Location1",
            numberOfCostumes = 3,
            partnerShops = emptyList(),
            detailedInfo = "Detailed product information",
            warrantyInfo = "blabla",
            contactInfo = "contact@example.com",
            images = listOf("image1.jpg"),
            options = emptyList(),
            createdAt = null,
            updatedAt = null,
        )
        val pagedResponse = PagedResponse(
            content = listOf(productResponse),
            page = 0,
            size = 10,
            totalElements = 1,
            totalPages = 1,
        )
        given(productUseCase.getMainPageProducts(0, 10)).willReturn(pagedResponse)

        val request = RestDocumentationRequestBuilders
            .get("/api/products/main")
            .param("page", "0")
            .param("size", "10")

        // when/then
        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "get-main-page-products",
                responseBody(
                    "success" type BOOLEAN means "응답 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "페이지 데이터",
                    "data.content" type ARRAY means "상품 목록",
                    "data.content[].productId" type NUMBER means "상품 ID",
                    "data.content[].userId" type NUMBER means "사용자 ID",
                    "data.content[].title" type STRING means "상품명",
                    "data.content[].description" type STRING means "상품 설명",
                    "data.content[].price" type NUMBER means "가격",
                    "data.content[].typeCode" type NUMBER means "상품 타입 코드",
                    "data.content[].shootingTime" type OBJECT means "촬영 시간",
                    "data.content[].shootingLocation" type STRING means "촬영 장소",
                    "data.content[].numberOfCostumes" type NUMBER means "의상 수",
                    "data.content[].partnerShops" type ARRAY means "협력업체 목록",
                    "data.content[].detailedInfo" type STRING means "상세 정보",
                    "data.content[].warrantyInfo" type STRING means "보증 정보",
                    "data.content[].contactInfo" type STRING means "연락처",
                    "data.content[].createdAt" type OBJECT means "생성 시간",
                    "data.content[].updatedAt" type OBJECT means "수정 시간",
                    "data.content[].options" type ARRAY means "옵션 목록",
                    "data.content[].images" type ARRAY means "상품 이미지 리스트",
                    "data.page" type NUMBER means "현재 페이지",
                    "data.size" type NUMBER means "페이지 크기",
                    "data.totalElements" type NUMBER means "전체 상품 수",
                    "data.totalPages" type NUMBER means "전체 페이지 수",
                ),
            )
    }

    @Test
    fun `상품 수정 API 테스트 - 정상 수정`() {
        // given
        val updateRequest = UpdateProductRequest(
            productId = 1L,
            title = "Updated Product",
            description = "Updated description",
            price = 15000,
            typeCode = 0,
            shootingTime = null,
            shootingLocation = "Location1",
            numberOfCostumes = 3,
            partnerShops = emptyList(),
            detailedInfo = "Updated detailed information",
            warrantyInfo = "Updated warranty",
            contactInfo = "updated@example.com",
            options = emptyList(),
            images = listOf("image1.jpg"),
        )
        val updatedProductResponse = ProductResponse(
            productId = 1L,
            userId = 1L,
            title = "Updated Product",
            description = "Updated description",
            price = 15000,
            typeCode = 0,
            shootingTime = null,
            shootingLocation = "Location1",
            numberOfCostumes = 3,
            partnerShops = emptyList(),
            detailedInfo = "Updated detailed information",
            warrantyInfo = "Updated warranty",
            contactInfo = "updated@example.com",
            images = listOf("image1.jpg"),
            options = emptyList(),
            createdAt = null,
            updatedAt = null,
        )
        given(productUseCase.updateProduct(updateRequest.copy(productId = 1L)))
            .willReturn(updatedProductResponse)

        val requestBuilder = RestDocumentationRequestBuilders
            .put("/api/products/{id}", 1L)
            .content(updateRequest.toJsonString())
            .contentType(MediaType.APPLICATION_JSON)

        // when/then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "update-product",
                responseBody(
                    "success" type BOOLEAN means "응답 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "업데이트된 상품 데이터",
                    "data.productId" type NUMBER means "상품 ID",
                    "data.userId" type NUMBER means "사용자 ID",
                    "data.title" type STRING means "상품명",
                    "data.description" type STRING means "상품 설명",
                    "data.price" type NUMBER means "가격",
                    "data.typeCode" type NUMBER means "상품 타입 코드",
                    "data.shootingTime" type OBJECT means "촬영 시간",
                    "data.shootingLocation" type STRING means "촬영 장소",
                    "data.numberOfCostumes" type NUMBER means "의상 수",
                    "data.partnerShops" type ARRAY means "협력업체 목록",
                    "data.detailedInfo" type STRING means "상세 정보",
                    "data.warrantyInfo" type STRING means "보증 정보",
                    "data.contactInfo" type STRING means "연락처",
                    "data.createdAt" type OBJECT means "생성 시간",
                    "data.updatedAt" type OBJECT means "수정 시간",
                    "data.options" type ARRAY means "옵션 목록",
                    "data.images" type ARRAY means "상품 이미지 리스트",
                ),
            )
    }

    @Test
    fun `상품 수정 API 테스트 - 존재하지 않는 상품`() {
        // given
        val updateRequest = UpdateProductRequest(
            productId = 999L,
            title = "Updated Product",
            description = "Updated description",
            price = 15000,
            typeCode = 0,
            shootingTime = null,
            shootingLocation = "Location1",
            numberOfCostumes = 3,
            partnerShops = emptyList(),
            detailedInfo = "Updated detailed information",
            warrantyInfo = "Updated warranty",
            contactInfo = "updated@example.com",
            options = emptyList(),
            images = listOf("image1.jpg"),
        )
        given(productUseCase.updateProduct(updateRequest))
            .willThrow(IllegalArgumentException("Product not found: 999"))

        val requestBuilder = RestDocumentationRequestBuilders
            .put("/api/products/{id}", 999L)
            .content(updateRequest.toJsonString())
            .contentType(MediaType.APPLICATION_JSON)

        // when/then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument("update-product-not-found")
    }

    @Test
    fun `상품 삭제 API 테스트 - 정상 삭제`() {
        // given
        org.mockito.Mockito.doNothing().`when`(productUseCase).deleteProduct(1L)

        val requestBuilder = RestDocumentationRequestBuilders
            .delete("/api/products/{id}", 1L)

        // when/then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isNoContent)
            .andDocument("delete-product")
    }

    @Test
    fun `상품 삭제 API 테스트 - 존재하지 않는 상품`() {
        // given
        org.mockito.Mockito.doThrow(IllegalArgumentException("The product to delete does not exist: 999."))
            .`when`(productUseCase).deleteProduct(999L)

        val requestBuilder = RestDocumentationRequestBuilders
            .delete("/api/products/{id}", 999L)

        // when/then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument("delete-product-not-found")
    }

    @Test
    fun `상품 단건 조회 API 테스트 - 정상 조회`() {
        // given
        val productResponse = ProductResponse(
            productId = 1L,
            userId = 1L,
            title = "New Product",
            description = "Product description",
            price = 10000,
            typeCode = 0,
            shootingTime = null,
            shootingLocation = "Location1",
            numberOfCostumes = 3,
            partnerShops = emptyList(),
            detailedInfo = "Detailed product information",
            warrantyInfo = "blabla",
            contactInfo = "contact@example.com",
            images = listOf("image1.jpg"),
            options = emptyList(),
            createdAt = null,
            updatedAt = null,
        )
        given(productUseCase.getProductById(1L)).willReturn(productResponse)

        val requestBuilder = RestDocumentationRequestBuilders
            .get("/api/products/{id}", 1L)

        // when/then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "get-product",
                responseBody(
                    "success" type BOOLEAN means "응답 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "상품 데이터",
                    "data.productId" type NUMBER means "상품 ID",
                    "data.userId" type NUMBER means "사용자 ID",
                    "data.title" type STRING means "상품명",
                    "data.description" type STRING means "상품 설명",
                    "data.price" type NUMBER means "가격",
                    "data.typeCode" type NUMBER means "상품 타입 코드",
                    "data.shootingTime" type OBJECT means "촬영 시간",
                    "data.shootingLocation" type STRING means "촬영 장소",
                    "data.numberOfCostumes" type NUMBER means "의상 수",
                    "data.partnerShops" type ARRAY means "협력업체 목록",
                    "data.detailedInfo" type STRING means "상세 정보",
                    "data.warrantyInfo" type STRING means "보증 정보",
                    "data.contactInfo" type STRING means "연락처",
                    "data.createdAt" type OBJECT means "생성 시간",
                    "data.updatedAt" type OBJECT means "수정 시간",
                    "data.options" type ARRAY means "옵션 목록",
                    "data.images" type ARRAY means "상품 이미지 리스트",
                ),
            )
    }

    @Test
    fun `상품 단건 조회 API 테스트 - 존재하지 않는 상품`() {
        // given
        given(productUseCase.getProductById(999L))
            .willThrow(IllegalArgumentException("Product with ID 999 not found."))

        val requestBuilder = RestDocumentationRequestBuilders
            .get("/api/products/{id}", 999L)

        // when/then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument(
                "get-product-not-found",
                responseBody(
                    "success" type BOOLEAN means "응답 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "에러 데이터 (null)",
                ),
            )
    }
}
