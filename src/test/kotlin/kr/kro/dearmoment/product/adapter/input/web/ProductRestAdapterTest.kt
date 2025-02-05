package kr.kro.dearmoment.product.adapter.input.web

import andDocument
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.restdocs.ARRAY
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.toJsonString
import kr.kro.dearmoment.common.restdocs.type
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
import kr.kro.dearmoment.product.domain.model.ConceptType
import kr.kro.dearmoment.product.domain.model.OriginalProvideType
import kr.kro.dearmoment.product.domain.model.SeasonHalf
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [ProductRestAdapter::class])
@Import(ProductRestAdapterTestConfig::class)
class ProductRestAdapterTest : RestApiTestBase() {
    @Autowired
    lateinit var productUseCase: ProductUseCase

    // --- 헬퍼: 공통 응답 문서 정의 ---
    private fun commonResponsePrefix() =
        arrayOf(
            "success" type BOOLEAN means "응답 성공 여부",
            "code" type NUMBER means "HTTP 상태 코드",
        )

    private fun productResponseDoc(prefix: String = "data") =
        arrayOf(
            "$prefix.productId" type NUMBER means "상품 ID",
            "$prefix.userId" type NUMBER means "사용자 ID",
            "$prefix.title" type STRING means "상품명",
            "$prefix.description" type STRING means "상품 설명",
            "$prefix.price" type NUMBER means "가격",
            "$prefix.typeCode" type NUMBER means "상품 타입 코드",
            "$prefix.concept" type STRING means "상품 콘셉트",
            "$prefix.originalProvideType" type STRING means "원본 제공 타입 (FULL/PARTIAL)",
            "$prefix.partialOriginalCount" type NUMBER means "PARTIAL 제공 시 제공할 원본 장수",
            "$prefix.shootingTime" type OBJECT means "촬영 시간",
            "$prefix.shootingLocation" type STRING means "촬영 장소",
            "$prefix.numberOfCostumes" type NUMBER means "의상 수",
            "$prefix.seasonYear" type NUMBER means "시즌 연도",
            "$prefix.seasonHalf" type STRING means "시즌 반(상/하반기)",
            "$prefix.partnerShops" type ARRAY means "협력업체 목록",
            "$prefix.detailedInfo" type STRING means "상세 정보",
            "$prefix.warrantyInfo" type STRING means "보증 정보",
            "$prefix.contactInfo" type STRING means "연락처",
            "$prefix.createdAt" type OBJECT means "생성 시간",
            "$prefix.updatedAt" type OBJECT means "수정 시간",
            "$prefix.images" type ARRAY means "상품 이미지 리스트",
        )

    private fun productOptionDoc(prefix: String = "data.options") =
        arrayOf(
            "$prefix[].optionId" type NUMBER means "옵션 ID",
            "$prefix[].productId" type NUMBER means "옵션에 소속된 상품 ID",
            "$prefix[].name" type STRING means "옵션명",
            "$prefix[].additionalPrice" type NUMBER means "추가 가격",
            "$prefix[].description" type STRING means "옵션 설명",
            "$prefix[].createdAt" type OBJECT means "옵션 생성 시간",
            "$prefix[].updatedAt" type OBJECT means "옵션 수정 시간",
        )

    private fun partnerShopDoc(prefix: String = "data.partnerShops") =
        arrayOf(
            "$prefix[].name" type STRING means "협력업체명",
            "$prefix[].link" type STRING means "협력업체 링크",
        )

    private fun paginationDoc(prefix: String = "data") =
        arrayOf(
            "$prefix.page" type NUMBER means "현재 페이지 번호",
            "$prefix.size" type NUMBER means "페이지 크기",
            "$prefix.totalElements" type NUMBER means "전체 상품 수",
            "$prefix.totalPages" type NUMBER means "전체 페이지 수",
        )

    // --- 헬퍼: 샘플 데이터 생성 ---
    private fun createSampleProductResponse(
        productId: Long = 1L,
        partnerShops: List<PartnerShopResponse> =
            listOf(
                PartnerShopResponse("Shop A", "http://shopA.com"),
                PartnerShopResponse("Shop B", "http://shopB.com"),
            ),
        options: List<ProductOptionResponse> =
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
    ): ProductResponse =
        ProductResponse(
            productId = productId,
            userId = 1L,
            title = if (productId == 1L) "New Product" else "Updated Product",
            description = if (productId == 1L) "Product description" else "Updated description",
            price = if (productId == 1L) 10000 else 15000,
            typeCode = 0,
            concept = ConceptType.ELEGANT,
            originalProvideType = OriginalProvideType.FULL,
            partialOriginalCount = null,
            shootingTime = null,
            shootingLocation = "Location1",
            numberOfCostumes = 3,
            seasonYear = 2023,
            seasonHalf = SeasonHalf.FIRST_HALF,
            partnerShops = partnerShops,
            detailedInfo = if (productId == 1L) "Detailed product information" else "Updated detailed information",
            warrantyInfo = if (productId == 1L) "blabla" else "Updated warranty",
            contactInfo = if (productId == 1L) "contact@example.com" else "updated@example.com",
            images = listOf("image1.jpg"),
            options = options,
            createdAt = null,
            updatedAt = null,
        )

    private fun createSamplePagedResponse(): PagedResponse<ProductResponse> {
        return PagedResponse(
            content = listOf(createSampleProductResponse()),
            page = 0,
            size = 10,
            totalElements = 1,
            totalPages = 1,
        )
    }

    // --- 테스트 케이스 ---

    @Test
    fun `상품 생성 API 테스트`() {
        // given
        val request =
            CreateProductRequest(
                userId = 1L,
                title = "New Product",
                price = 10000,
                typeCode = 0,
                concept = ConceptType.ELEGANT,
                originalProvideType = OriginalProvideType.FULL,
                partialOriginalCount = null,
                shootingTime = null,
                shootingLocation = "Location1",
                numberOfCostumes = 3,
                seasonYear = 2023,
                seasonHalf = SeasonHalf.FIRST_HALF,
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
                partnerShops =
                    listOf(
                        CreatePartnerShopRequest("Shop A", "http://shopA.com"),
                        CreatePartnerShopRequest("Shop B", "http://shopB.com"),
                    ),
                warrantyInfo = "Warranty Description",
            )

        val productResponse = createSampleProductResponse()

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
                    *commonResponsePrefix(),
                    *productResponseDoc("data"),
                    *partnerShopDoc("data.partnerShops"),
                    *productOptionDoc("data.options"),
                ),
            )
    }

    @Test
    fun `메인페이지 상품 조회 API 테스트`() {
        // given
        val pagedResponse = createSamplePagedResponse()
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
                    *commonResponsePrefix(),
                    // 페이징 구조 내에 content 항목으로 상품 목록 정의
                    "data.content[]" type ARRAY means "상품 목록",
                    *productResponseDoc("data.content[]"),
                    *partnerShopDoc("data.content[].partnerShops"),
                    *productOptionDoc("data.content[].options"),
                    // 페이징 관련 필드
                    *paginationDoc("data"),
                ),
            )
    }

    @Test
    fun `상품 수정 API 테스트 - 정상 수정 (옵션 삭제 포함)`() {
        // given
        val updateRequest =
            UpdateProductRequest(
                userId = 1L,
                productId = 1L,
                title = "Updated Product",
                description = "Updated description",
                price = 15000,
                typeCode = 0,
                concept = ConceptType.ELEGANT,
                originalProvideType = OriginalProvideType.FULL,
                partialOriginalCount = null,
                shootingTime = null,
                shootingLocation = "Location1",
                numberOfCostumes = 3,
                seasonYear = 2023,
                seasonHalf = SeasonHalf.FIRST_HALF,
                partnerShops = emptyList(),
                detailedInfo = "Updated detailed information",
                warrantyInfo = "Updated warranty",
                contactInfo = "updated@example.com",
                options =
                    listOf(
                        // 기존 옵션 업데이트
                        UpdateProductOptionRequest(
                            optionId = 1L,
                            name = "Option 1 Updated",
                            additionalPrice = 1200,
                            description = "Extra option updated",
                        ),
                        // 신규 옵션 추가
                        UpdateProductOptionRequest(
                            optionId = null,
                            name = "New Option",
                            additionalPrice = 500,
                            description = "Additional new option",
                        ),
                    ),
                images = listOf("image1.jpg"),
            )

        val updatedProductResponse =
            createSampleProductResponse(
                productId = 1L,
                partnerShops = emptyList(),
                options =
                    listOf(
                        ProductOptionResponse(
                            optionId = 1L,
                            productId = 1L,
                            name = "Option 1 Updated",
                            additionalPrice = 1200,
                            description = "Extra option updated",
                            createdAt = null,
                            updatedAt = null,
                        ),
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
                    *commonResponsePrefix(),
                    *productResponseDoc("data"),
                    *partnerShopDoc("data.partnerShops"),
                    *productOptionDoc("data.options"),
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
                concept = ConceptType.ELEGANT,
                originalProvideType = OriginalProvideType.FULL,
                partialOriginalCount = null,
                shootingTime = null,
                shootingLocation = "Location1",
                numberOfCostumes = 3,
                seasonYear = 2023,
                seasonHalf = SeasonHalf.FIRST_HALF,
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
            .`when`(productUseCase)
            .deleteProduct(999L)

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
        val productResponse = createSampleProductResponse()
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
                    *commonResponsePrefix(),
                    *productResponseDoc("data"),
                    *partnerShopDoc("data.partnerShops"),
                    // options가 단순 배열일 경우엔 아래와 같이 문서화
                    *productOptionDoc("data.options"),
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
                    *commonResponsePrefix(),
                    "data" type OBJECT means "에러 데이터 (null)",
                ),
            )
    }
}
