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
import kr.kro.dearmoment.product.application.dto.request.CreatePartnerShopRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.PartnerShopResponse
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
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
@Import(ProductRestAdapterTestConfig::class)
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
        val request =
            CreateProductRequest(
                userId = 1L,
                title = "New Product",
                price = 10000,
                typeCode = 0,
                images = listOf("image1.jpg"),
                options =
                    listOf(
                        CreateProductOptionRequest(
                            optionId = 1L,
                            name = "Option 1",
                            additionalPrice = 1000,
                            description = "Extra option",
                        ),
                    ),
                contactInfo = "contact@example.com",
                description = "Product description",
                detailedInfo = "Detailed product information",
                numberOfCostumes = 3,
                partnerShops =
                    listOf(
                        CreatePartnerShopRequest(
                            name = "Shop A",
                            link = "http://shopA.com",
                        ),
                        CreatePartnerShopRequest(
                            name = "Shop B",
                            link = "http://shopB.com",
                        ),
                    ),
                shootingLocation = "Location1",
                shootingTime = null,
                warrantyInfo = "blabla",
            )
        val productResponse =
            ProductResponse(
                productId = 1L,
                userId = 1L,
                title = "New Product",
                description = "Product description",
                price = 10000,
                typeCode = 0,
                shootingTime = null,
                shootingLocation = "Location1",
                numberOfCostumes = 3,
                partnerShops =
                    listOf(
                        PartnerShopResponse(
                            name = "Shop A",
                            link = "http://shopA.com",
                        ),
                        PartnerShopResponse(
                            name = "Shop B",
                            link = "http://shopB.com",
                        ),
                    ),
                detailedInfo = "Detailed product information",
                warrantyInfo = "blabla",
                contactInfo = "contact@example.com",
                images = listOf("image1.jpg"),
                options =
                    listOf(
                        ProductOptionResponse(
                            optionId = 1L,
                            productId = 1L,
                            name = "Option 1",
                            additionalPrice = 1000,
                            description = "Extra option",
                            createdAt = null,
                            updatedAt = null,
                        ),
                    ),
                createdAt = null,
                updatedAt = null,
            )
        // 실제 컨트롤러에서는 단일 상품 객체를 반환함
        given(productUseCase.saveProduct(request)).willReturn(productResponse)

        val requestBuilder =
            RestDocumentationRequestBuilders
                .post("/api/products")
                .content(request.toJsonString())
                .contentType(MediaType.APPLICATION_JSON)

        // when/then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isCreated)
            .andDocument(
                "create-product",
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
                    "data.partnerShops[].name" type STRING means "협력업체명",
                    "data.partnerShops[].link" type STRING means "협력업체 링크",
                    "data.detailedInfo" type STRING means "상세 정보",
                    "data.warrantyInfo" type STRING means "보증 정보",
                    "data.contactInfo" type STRING means "연락처",
                    "data.createdAt" type OBJECT means "생성 시간",
                    "data.updatedAt" type OBJECT means "수정 시간",
                    "data.options" type ARRAY means "옵션 목록",
                    "data.options[].optionId" type NUMBER means "옵션 ID",
                    "data.options[].productId" type NUMBER means "옵션에 소속된 상품 ID",
                    "data.options[].name" type STRING means "옵션명",
                    "data.options[].additionalPrice" type NUMBER means "추가 가격",
                    "data.options[].description" type STRING means "옵션 설명",
                    "data.options[].createdAt" type OBJECT means "옵션 생성 시간",
                    "data.options[].updatedAt" type OBJECT means "옵션 수정 시간",
                    "data.images" type ARRAY means "상품 이미지 리스트",
                ),
            )
    }

    @Test
    fun `메인페이지 상품 조회 API 테스트`() {
        // given
        val productResponse =
            ProductResponse(
                productId = 1L,
                userId = 1L,
                title = "New Product",
                description = "Product description",
                price = 10000,
                typeCode = 0,
                shootingTime = null,
                shootingLocation = "Location1",
                numberOfCostumes = 3,
                partnerShops =
                    listOf(
                        PartnerShopResponse(
                            name = "Shop A",
                            link = "http://shopA.com",
                        ),
                        PartnerShopResponse(
                            name = "Shop B",
                            link = "http://shopB.com",
                        ),
                    ),
                detailedInfo = "Detailed product information",
                warrantyInfo = "blabla",
                contactInfo = "contact@example.com",
                images = listOf("image1.jpg"),
                options =
                    listOf(
                        ProductOptionResponse(
                            optionId = 1L,
                            productId = 1L,
                            name = "Option 1",
                            additionalPrice = 1000,
                            description = "Extra option",
                            createdAt = null,
                            updatedAt = null,
                        ),
                    ),
                createdAt = null,
                updatedAt = null,
            )
        val pagedResponse =
            PagedResponse(
                content = listOf(productResponse),
                page = 0,
                size = 10,
                totalElements = 1,
                totalPages = 1,
            )
        given(productUseCase.getMainPageProducts(0, 10)).willReturn(pagedResponse)

        val request =
            RestDocumentationRequestBuilders
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
                    "data" type OBJECT means "상품 데이터 (페이징 구조)",
                    "data.content[]" type ARRAY means "상품 목록",
                    "data.content[].productId" type NUMBER means "상품 ID",
                    "data.content[].userId" type NUMBER means "사용자 ID",
                    "data.content[].title" type STRING means "상품명",
                    "data.content[].description" type STRING means "상품 설명",
                    "data.content[].price" type NUMBER means "가격",
                    "data.content[].typeCode" type NUMBER means "상품 타입 코드",
                    "data.content[].shootingTime" type OBJECT means "촬영 시간",
                    "data.content[].shootingLocation" type STRING means "촬영 장소",
                    "data.content[].numberOfCostumes" type NUMBER means "의상 수",
                    "data.content[].partnerShops[]" type ARRAY means "협력업체 목록",
                    "data.content[].partnerShops[].name" type STRING means "협력업체명",
                    "data.content[].partnerShops[].link" type STRING means "협력업체 링크",
                    "data.content[].detailedInfo" type STRING means "상세 정보",
                    "data.content[].warrantyInfo" type STRING means "보증 정보",
                    "data.content[].contactInfo" type STRING means "연락처",
                    "data.content[].createdAt" type OBJECT means "생성 시간",
                    "data.content[].updatedAt" type OBJECT means "수정 시간",
                    "data.content[].options[]" type ARRAY means "옵션 목록",
                    "data.content[].options[].optionId" type NUMBER means "옵션 ID",
                    "data.content[].options[].productId" type NUMBER means "옵션에 소속된 상품 ID",
                    "data.content[].options[].name" type STRING means "옵션명",
                    "data.content[].options[].additionalPrice" type NUMBER means "추가 가격",
                    "data.content[].options[].description" type STRING means "옵션 설명",
                    "data.content[].options[].createdAt" type OBJECT means "옵션 생성 시간",
                    "data.content[].options[].updatedAt" type OBJECT means "옵션 수정 시간",
                    "data.content[].images" type ARRAY means "상품 이미지 리스트",
                    "data.page" type NUMBER means "현재 페이지 번호",
                    "data.size" type NUMBER means "페이지 크기",
                    "data.totalElements" type NUMBER means "전체 상품 수",
                    "data.totalPages" type NUMBER means "전체 페이지 수",
                ),
            )
    }

    @Test
    fun `상품 수정 API 테스트 - 정상 수정 (옵션 삭제 포함)`() {
        // given
        // 업데이트 요청: 기존 옵션 중 옵션 1은 업데이트하고, 신규 옵션 추가 (옵션 삭제 대상: 옵션 2, 옵션 3)
        val updateRequest =
            UpdateProductRequest(
                userId = 1L,
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
                options =
                    listOf(
                        // 기존 옵션 업데이트 (이미 존재하는 옵션: 옵션 1)
                        UpdateProductOptionRequest(
                            optionId = 1L,
                            name = "Option 1 Updated",
                            additionalPrice = 1200,
                            description = "Extra option updated",
                        ),
                        // 신규 옵션 추가 (optionId가 null이면 신규 옵션으로 추가)
                        UpdateProductOptionRequest(
                            optionId = null,
                            name = "New Option",
                            additionalPrice = 500,
                            description = "Additional new option",
                        ),
                    ),
                images = listOf("image1.jpg"),
            )

        // 기존 상품의 옵션은 옵션 1, 옵션 2, 옵션 3이 있었던 것으로 가정합니다.
        // 업데이트 후 응답에서는 옵션 1(업데이트된 결과)과 신규 추가된 옵션만 포함되어야 합니다.
        val updatedProductResponse =
            ProductResponse(
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
                options =
                    listOf(
                        // 기존 옵션 업데이트 결과 (옵션 1)
                        ProductOptionResponse(
                            optionId = 1L,
                            productId = 1L,
                            name = "Option 1 Updated",
                            additionalPrice = 1200,
                            description = "Extra option updated",
                            createdAt = null,
                            updatedAt = null,
                        ),
                        // 신규 추가된 옵션 (DB에서 부여된 ID 예시: 4L)
                        ProductOptionResponse(
                            optionId = 4L,
                            productId = 1L,
                            name = "New Option",
                            additionalPrice = 500,
                            description = "Additional new option",
                            createdAt = null,
                            updatedAt = null,
                        ),
                    ),
                createdAt = null,
                updatedAt = null,
            )

        given(productUseCase.updateProduct(updateRequest.copy(productId = 1L)))
            .willReturn(updatedProductResponse)

        val requestBuilder =
            RestDocumentationRequestBuilders
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
                    "data.options[].optionId" type NUMBER means "옵션 ID",
                    "data.options[].productId" type NUMBER means "옵션에 소속된 상품 ID",
                    "data.options[].name" type STRING means "옵션명",
                    "data.options[].additionalPrice" type NUMBER means "추가 가격",
                    "data.options[].description" type STRING means "옵션 설명",
                    "data.options[].createdAt" type OBJECT means "옵션 생성 시간",
                    "data.options[].updatedAt" type OBJECT means "옵션 수정 시간",
                    "data.images" type ARRAY means "상품 이미지 리스트",
                ),
            )
    }

    @Test
    fun `상품 수정 API 테스트 - 존재하지 않는 상품`() {
        // given
        val updateRequest =
            UpdateProductRequest(
                userId = 1L,
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

        val requestBuilder =
            RestDocumentationRequestBuilders
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

        val requestBuilder =
            RestDocumentationRequestBuilders
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

        val requestBuilder =
            RestDocumentationRequestBuilders
                .delete("/api/products/{id}", 999L)

        // when/then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument("delete-product-not-found")
    }

    @Test
    fun `상품 단건 조회 API 테스트 - 정상 조회`() {
        // given
        val productResponse =
            ProductResponse(
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

        val requestBuilder =
            RestDocumentationRequestBuilders
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

        val requestBuilder =
            RestDocumentationRequestBuilders
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
