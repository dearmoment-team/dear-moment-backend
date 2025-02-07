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
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.domain.model.SeasonHalf
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@WebMvcTest(controllers = [ProductRestAdapter::class])
@Import(ProductRestAdapterTestConfig::class)
class ProductRestAdapterTest : RestApiTestBase() {

    @Autowired
    lateinit var productUseCase: ProductUseCase

    // --- 헬퍼: 공통 응답 문서 정의 ---
    private fun commonResponsePrefix() = arrayOf(
        "success" type BOOLEAN means "응답 성공 여부",
        "code" type NUMBER means "HTTP 상태 코드"
    )

    private fun productResponseDoc(prefix: String = "data") = arrayOf(
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
        "$prefix.images" type ARRAY means "상품 이미지 리스트"
    )

    private fun productOptionDoc(prefix: String = "data.options") = arrayOf(
        "$prefix[].optionId" type NUMBER means "옵션 ID",
        "$prefix[].productId" type NUMBER means "옵션에 소속된 상품 ID",
        "$prefix[].name" type STRING means "옵션명",
        "$prefix[].additionalPrice" type NUMBER means "추가 가격",
        "$prefix[].description" type STRING means "옵션 설명",
        "$prefix[].createdAt" type OBJECT means "옵션 생성 시간",
        "$prefix[].updatedAt" type OBJECT means "옵션 수정 시간"
    )

    private fun partnerShopDoc(prefix: String = "data.partnerShops") = arrayOf(
        "$prefix[].name" type STRING means "협력업체명",
        "$prefix[].link" type STRING means "협력업체 링크"
    )

    private fun paginationDoc(prefix: String = "data") = arrayOf(
        "$prefix.page" type NUMBER means "현재 페이지 번호",
        "$prefix.size" type NUMBER means "페이지 크기",
        "$prefix.totalElements" type NUMBER means "전체 상품 수",
        "$prefix.totalPages" type NUMBER means "전체 페이지 수"
    )

    // --- 헬퍼: 샘플 데이터 생성 ---
    private fun createSampleProductResponse(
        productId: Long = 1L,
        partnerShops: List<PartnerShopResponse> = listOf(
            PartnerShopResponse("Shop A", "http://shopA.com"),
            PartnerShopResponse("Shop B", "http://shopB.com")
        ),
        options: List<ProductOptionResponse> = listOf(
            ProductOptionResponse(
                optionId = 1L,
                productId = 1L,
                name = "Option 1",
                additionalPrice = 1000,
                description = "Extra option",
                createdAt = null,
                updatedAt = null
            )
        )
    ): ProductResponse = ProductResponse(
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
        updatedAt = null
    )

    private fun createSamplePagedResponse(): PagedResponse<ProductResponse> {
        return PagedResponse(
            content = listOf(createSampleProductResponse()),
            page = 0,
            size = 10,
            totalElements = 1,
            totalPages = 1
        )
    }

    // --- 헬퍼 함수: 도메인 모델 생성 ---
    private fun createProductOption(
        optionId: Long = 0L,
        productId: Long = 0L,
        name: String = "Option",
        additionalPrice: Long = 0,
        description: String = ""
    ): ProductOption {
        return ProductOption(
            optionId = optionId,
            productId = productId,
            name = name,
            additionalPrice = additionalPrice,
            description = description
        )
    }

    private fun createProduct(
        productId: Long = 1L,
        userId: Long = 1L,
        title: String = "Sample Product",
        description: String = "Sample Description",
        price: Long = 10000,
        typeCode: Int = 0,
        concept: ConceptType = ConceptType.ELEGANT,
        originalProvideType: OriginalProvideType = OriginalProvideType.FULL,
        partialOriginalCount: Int? = null,
        shootingTime: LocalDateTime? = null,
        shootingLocation: String = "",
        numberOfCostumes: Int = 0,
        seasonYear: Int? = null,
        seasonHalf: SeasonHalf? = null,
        partnerShops: List<PartnerShop> = emptyList(),
        detailedInfo: String = "",
        warrantyInfo: String = "",
        contactInfo: String = "",
        options: List<ProductOption> = emptyList(),
        images: List<String> = listOf("image1.jpg", "image2.jpg")
    ): Product {
        return Product(
            productId = productId,
            userId = userId,
            title = title,
            description = description,
            price = price,
            typeCode = typeCode,
            concept = concept,
            originalProvideType = originalProvideType,
            partialOriginalCount = partialOriginalCount,
            shootingTime = shootingTime,
            shootingLocation = shootingLocation,
            numberOfCostumes = numberOfCostumes,
            seasonYear = seasonYear,
            seasonHalf = seasonHalf,
            partnerShops = partnerShops,
            detailedInfo = detailedInfo,
            warrantyInfo = warrantyInfo,
            contactInfo = contactInfo,
            options = options,
            images = images
        )
    }

    // --- 테스트 케이스 ---

    @Test
    fun `상품 생성 API 테스트`() {
        // given
        val request = CreateProductRequest(
            userId = 1L,
            title = "New Product",
            description = "New description",
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
            partnerShops = listOf(
                CreatePartnerShopRequest("Shop A", "http://shopA.com"),
                CreatePartnerShopRequest("Shop B", "http://shopB.com")
            ),
            detailedInfo = "Detailed product information",
            warrantyInfo = "Warranty Description",
            contactInfo = "contact@example.com",
            options = listOf(
                CreateProductOptionRequest(name = "Option 1", additionalPrice = 1000, description = "Extra option")
            )
        )

        val productResponse = createSampleProductResponse()

        // 이미지 파라미터는 실제 MultipartFile 인스턴스로 전달되므로, any()를 사용합니다.
        given(productUseCase.saveProduct(any(), any<List<MultipartFile>>()))
            .willReturn(productResponse)

        // Create multipart request with two parts: "request" (JSON) and "images" (빈 파일 리스트)
        val requestPart = MockMultipartFile("request", "", "application/json", request.toJsonString().toByteArray())
        // 빈 이미지 파일이지만, MultipartFile 인스턴스로 생성합니다.
        val imagesPart = MockMultipartFile("images", "image1.jpg", "image/jpeg", ByteArray(0))
        val requestBuilder = RestDocumentationRequestBuilders.multipart("/api/products")
            .file(requestPart)
            .file(imagesPart)

        // when/then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isCreated)
            .andDocument(
                "create-product",
                responseBody(
                    *commonResponsePrefix(),
                    *productResponseDoc("data"),
                    *partnerShopDoc("data.partnerShops"),
                    *productOptionDoc("data.options")
                )
            )
    }

    @Test
    fun `메인페이지 상품 조회 API 테스트`() {
        // given
        val pagedResponse = createSamplePagedResponse()
        given(productUseCase.getMainPageProducts(0, 10)).willReturn(pagedResponse)

        val requestBuilder = RestDocumentationRequestBuilders.get("/api/products/main")
            .param("page", "0")
            .param("size", "10")

        // when/then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "get-main-page-products",
                responseBody(
                    *commonResponsePrefix(),
                    "data.content[]" type ARRAY means "상품 목록",
                    *productResponseDoc("data.content[]"),
                    *partnerShopDoc("data.content[].partnerShops"),
                    *productOptionDoc("data.content[].options"),
                    *paginationDoc("data")
                )
            )
    }

    @Test
    fun `상품 수정 API 테스트 - 정상 수정 (옵션 삭제 포함)`() {
        // given
        val updateRequest = UpdateProductRequest(
            productId = 1L,
            userId = 1L,
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
            options = listOf(
                // 기존 옵션 업데이트
                UpdateProductOptionRequest(
                    optionId = 1L,
                    name = "Option 1 Updated",
                    additionalPrice = 1200,
                    description = "Extra option updated"
                ),
                // 신규 옵션 추가
                UpdateProductOptionRequest(
                    optionId = null,
                    name = "New Option",
                    additionalPrice = 500,
                    description = "Additional new option"
                )
            ),
            images = listOf("image1.jpg", "new_image.jpg")
        )

        // 업데이트 전 기존 옵션: "Old Option"과 "To Delete"
        val existingOptions = listOf(
            createProductOption(optionId = 1L, productId = 1L, name = "Old Option", additionalPrice = 1000),
            createProductOption(optionId = 2L, productId = 1L, name = "To Delete", additionalPrice = 2000)
        )

        // 기존 제품 도메인 (업데이트 전)
        val existingProductDomain = createProduct(
            productId = 1L,
            userId = 1L,
            title = "Original Product",
            description = "Original Description",
            price = 10000,
            options = existingOptions,
            images = listOf("image1.jpg", "image2.jpg")
        )

        // 신규 이미지 처리를 위한 Mock MultipartFile
        val newImage = MockMultipartFile("images", "new_image.jpg", "image/jpeg", "fakeImageContent".toByteArray())

        // ImageService 모킹: 신규 이미지 업로드 시 save() 반환값 100L, getOne(100L) 반환 GetImageResponse with fileName "new_image.jpg"
        given(productUseCase.uploadImages(any(), any())).willReturn(listOf("new_image.jpg"))
        // updateProduct 호출 시, 업데이트 후 응답을 반환하도록 설정
        val updatedOptions = listOf(
            createProductOption(optionId = 1L, productId = 1L, name = "Option 1 Updated", additionalPrice = 1200),
            createProductOption(optionId = 4L, productId = 1L, name = "New Option", additionalPrice = 500)
        )
        given(productUseCase.updateProduct(any(), any())).willReturn(
            createSampleProductResponse(
                productId = 1L,
                partnerShops = emptyList(),
                options = updatedOptions.map { opt ->
                    ProductOptionResponse(
                        optionId = opt.optionId,
                        productId = opt.productId,
                        name = opt.name,
                        additionalPrice = opt.additionalPrice,
                        description = opt.description,
                        createdAt = null,
                        updatedAt = null
                    )
                }
            )
        )

        // Create multipart request for update: 두 파트("request"와 "images"), HTTP 메서드는 PUT으로 설정
        val requestPart = MockMultipartFile("request", "", "application/json", updateRequest.toJsonString().toByteArray())
        val requestBuilder = RestDocumentationRequestBuilders.multipart("/api/products/{id}", 1L)
            .file(requestPart)
            .file(newImage)
            .with { req ->
                req.method = "PUT"
                req
            }

        // when/then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "update-product",
                responseBody(
                    *commonResponsePrefix(),
                    *productResponseDoc("data"),
                    *partnerShopDoc("data.partnerShops"),
                    *productOptionDoc("data.options")
                )
            )
    }

    @Test
    fun `상품 수정 API 테스트 - 존재하지 않는 상품`() {
        // given
        val updateRequest = UpdateProductRequest(
            productId = 999L,
            userId = 1L,
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
            images = listOf("image1.jpg")
        )
        given(productUseCase.updateProduct(any())).willThrow(IllegalArgumentException("Product not found: 999"))

        val requestPart = MockMultipartFile("request", "", "application/json", updateRequest.toJsonString().toByteArray())
        val requestBuilder = RestDocumentationRequestBuilders.multipart("/api/products/{id}", 999L)
            .file(requestPart)
            .with { req -> req.method = "PUT"; req }

        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument("update-product-not-found")
    }

    @Test
    fun `상품 삭제 API 테스트 - 정상 삭제`() {
        org.mockito.Mockito.doNothing().`when`(productUseCase).deleteProduct(1L)

        val requestBuilder = RestDocumentationRequestBuilders.delete("/api/products/{id}", 1L)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isNoContent)
            .andDocument("delete-product")
    }

    @Test
    fun `상품 삭제 API 테스트 - 존재하지 않는 상품`() {
        org.mockito.Mockito.doThrow(IllegalArgumentException("The product to delete does not exist: 999."))
            .`when`(productUseCase)
            .deleteProduct(999L)

        val requestBuilder = RestDocumentationRequestBuilders.delete("/api/products/{id}", 999L)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument("delete-product-not-found")
    }

    @Test
    fun `상품 단건 조회 API 테스트 - 정상 조회`() {
        val productResponse = createSampleProductResponse()
        given(productUseCase.getProductById(any())).willReturn(productResponse)

        val requestBuilder = RestDocumentationRequestBuilders.get("/api/products/{id}", 1L)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "get-product",
                responseBody(
                    *commonResponsePrefix(),
                    *productResponseDoc("data"),
                    *partnerShopDoc("data.partnerShops"),
                    *productOptionDoc("data.options")
                )
            )
    }

    @Test
    fun `상품 단건 조회 API 테스트 - 존재하지 않는 상품`() {
        given(productUseCase.getProductById(any()))
            .willThrow(IllegalArgumentException("Product with ID 999 not found."))

        val requestBuilder = RestDocumentationRequestBuilders.get("/api/products/{id}", 999L)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument(
                "get-product-not-found",
                responseBody(
                    *commonResponsePrefix(),
                    "data" type OBJECT means "에러 데이터 (null)"
                )
            )
    }

    @Test
    fun `searchProducts 테스트`() {
        given(productUseCase.searchProducts("test", 10000, 30000, sortBy = "createdAt", page = 0, size = 10))
            .willReturn(
                PagedResponse(
                    content = listOf(
                        createSampleProductResponse(productId = 1L),
                        createSampleProductResponse(productId = 2L),
                        createSampleProductResponse(productId = 3L)
                    ),
                    page = 0,
                    size = 10,
                    totalElements = 3,
                    totalPages = 1
                )
            )

        val requestBuilder = RestDocumentationRequestBuilders.get("/api/products/search")
            .param("title", "test")
            .param("minPrice", "10000")
            .param("maxPrice", "30000")
            .param("sortBy", "createdAt")
            .param("page", "0")
            .param("size", "10")

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "search-products",
                responseBody(
                    *commonResponsePrefix(),
                    "data.content[]" type ARRAY means "상품 목록",
                    *productResponseDoc("data.content[]"),
                    *partnerShopDoc("data.content[].partnerShops"),
                    *productOptionDoc("data.content[].options"),
                    *paginationDoc("data")
                )
            )
    }
}