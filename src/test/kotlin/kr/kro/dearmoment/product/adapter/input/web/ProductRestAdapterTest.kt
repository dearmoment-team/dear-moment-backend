package kr.kro.dearmoment.product.adapter.input.web

import andDocument
import com.fasterxml.jackson.databind.ObjectMapper
import kr.kro.dearmoment.common.restdocs.ARRAY
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.product.application.dto.request.CreatePartnerShopRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.ImageReference
import kr.kro.dearmoment.product.application.dto.request.UpdatePartnerShopRequest
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
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@WebMvcTest(controllers = [ProductRestAdapter::class])
@Import(ProductRestAdapterTestConfig::class)
@AutoConfigureRestDocs
class ProductRestAdapterTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var productUseCase: ProductUseCase

    @Test
    fun `상품 생성 API 테스트`() {
        // 현재 시간
        val now = LocalDateTime.now()

        // given: 요청 객체 생성 (모든 필드를 채움)
        val request =
            CreateProductRequest(
                userId = 1L,
                title = "New Product",
                description = "Product description for New Product",
                price = 10000,
                typeCode = 0,
                concept = ConceptType.MODERN,
                originalProvideType = OriginalProvideType.FULL,
                partialOriginalCount = 0,
                shootingTime = now,
                shootingLocation = "Location1",
                numberOfCostumes = 3,
                seasonYear = 2025,
                seasonHalf = SeasonHalf.FIRST_HALF,
                partnerShops =
                listOf(
                    CreatePartnerShopRequest(name = "Shop A", link = "http://shopA.com"),
                    CreatePartnerShopRequest(name = "Shop B", link = "http://shopB.com"),
                ),
                detailedInfo = "Detailed product information",
                warrantyInfo = "Warranty Description",
                contactInfo = "contact@example.com",
                options =
                listOf(
                    CreateProductOptionRequest(
                        optionId = 1L,
                        name = "Option 1",
                        additionalPrice = 1000,
                        description = "Extra option details",
                    ),
                ),
            )

        // given: 응답 객체 생성 (요청과 연관된 모든 필드를 채움)
        val productResponse =
            ProductResponse(
                productId = 1L,
                userId = request.userId,
                title = request.title,
                description = request.description,
                price = request.price,
                typeCode = request.typeCode,
                concept = request.concept,
                originalProvideType = request.originalProvideType,
                partialOriginalCount = request.partialOriginalCount,
                shootingTime = request.shootingTime,
                shootingLocation = request.shootingLocation,
                numberOfCostumes = request.numberOfCostumes,
                seasonYear = request.seasonYear,
                seasonHalf = request.seasonHalf,
                partnerShops =
                listOf(
                    PartnerShopResponse(name = "Shop A", link = "http://shopA.com"),
                    PartnerShopResponse(name = "Shop B", link = "http://shopB.com"),
                ),
                detailedInfo = request.detailedInfo,
                warrantyInfo = request.warrantyInfo,
                contactInfo = request.contactInfo,
                createdAt = now,
                updatedAt = now,
                options =
                listOf(
                    ProductOptionResponse(
                        optionId = 1L,
                        productId = 1L,
                        name = "Option 1",
                        additionalPrice = 1000,
                        description = "Extra option details",
                        createdAt = now,
                        updatedAt = now,
                    ),
                ),
                // API 응답에서는 이미지 목록은 fileName 문자열 리스트로 전달합니다.
                images = listOf("image1.jpg")
            )

        // given: 실제 이미지 파일 객체 생성
        val imageFile =
            MockMultipartFile(
                "images",
                "image1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy image content".toByteArray(),
            )
        val imagesList: List<MultipartFile> = listOf(imageFile)

        // given: productUseCase.saveProduct를 실제 객체와 함께 stubbing
        given(productUseCase.saveProduct(request, imagesList)).willReturn(productResponse)

        // 요청 JSON 파트 생성: objectMapper를 사용하여 request 객체를 JSON 문자열로 변환합니다.
        val requestJson = objectMapper.writeValueAsString(request)
        val requestPart =
            MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson.toByteArray(),
            )

        // 요청 빌더: multipart 요청 구성 (JSON 파트와 이미지 파일 파트를 함께 전송)
        val requestBuilder =
            MockMvcRequestBuilders.multipart("/api/products")
                .file(requestPart)
                .file(imageFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON)

        // when/then: 요청 수행, 응답 검증 및 API 문서 생성
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
                    "data.concept" type STRING means "콘셉트",
                    "data.originalProvideType" type STRING means "원본 제공 타입",
                    "data.partialOriginalCount" type NUMBER means "제공할 원본 장수",
                    "data.shootingTime" type STRING means "촬영 시간",
                    "data.shootingLocation" type STRING means "촬영 장소",
                    "data.numberOfCostumes" type NUMBER means "의상 수",
                    "data.seasonYear" type NUMBER means "시즌 연도",
                    "data.seasonHalf" type STRING means "상반기/하반기",
                    "data.partnerShops" type ARRAY means "협력업체 목록",
                    "data.partnerShops[].name" type STRING means "협력업체명",
                    "data.partnerShops[].link" type STRING means "협력업체 링크",
                    "data.detailedInfo" type STRING means "상세 정보",
                    "data.warrantyInfo" type STRING means "보증 정보",
                    "data.contactInfo" type STRING means "연락처",
                    "data.createdAt" type STRING means "생성 시간",
                    "data.updatedAt" type STRING means "수정 시간",
                    "data.options" type ARRAY means "옵션 목록",
                    "data.options[].optionId" type NUMBER means "옵션 ID",
                    "data.options[].productId" type NUMBER means "옵션에 소속된 상품 ID",
                    "data.options[].name" type STRING means "옵션명",
                    "data.options[].additionalPrice" type NUMBER means "추가 가격",
                    "data.options[].description" type STRING means "옵션 설명",
                    "data.options[].createdAt" type STRING means "옵션 생성 시간",
                    "data.options[].updatedAt" type STRING means "옵션 수정 시간",
                    "data.images" type ARRAY means "상품 이미지 리스트",
                ),
            )
    }

    @Test
    fun `상품 수정 API 테스트`() {
        // 현재 시간
        val now = LocalDateTime.now()

        // given: 수정 요청 객체 생성 (모든 필드를 채움)
        val updateRequest =
            UpdateProductRequest(
                productId = 1L,
                userId = 1L,
                title = "Updated Product",
                description = "Updated product description",
                price = 15000,
                typeCode = 0,
                concept = ConceptType.MODERN,
                originalProvideType = OriginalProvideType.FULL,
                partialOriginalCount = 0,
                shootingTime = now,
                shootingLocation = "Updated Location",
                numberOfCostumes = 4,
                seasonYear = 2025,
                seasonHalf = SeasonHalf.SECOND_HALF,
                partnerShops =
                listOf(
                    UpdatePartnerShopRequest(name = "Shop A Updated", link = "http://shopA-updated.com"),
                    UpdatePartnerShopRequest(name = "Shop B Updated", link = "http://shopB-updated.com"),
                ),
                detailedInfo = "Updated detailed product information",
                warrantyInfo = "Updated Warranty Description",
                contactInfo = "updated_contact@example.com",
                options =
                listOf(
                    UpdateProductOptionRequest(
                        optionId = 1L,
                        name = "Updated Option 1",
                        additionalPrice = 2000,
                        description = "Updated extra option details",
                    ),
                ),
                // images는 이제 List<ImageReference> 타입입니다.
                images = listOf(ImageReference("updated_image1.jpg"))
            )

        // given: 수정 후 반환될 응답 객체 생성 (updateRequest의 필드를 반영)
        val productResponse =
            ProductResponse(
                productId = updateRequest.productId,
                userId = updateRequest.userId,
                title = updateRequest.title,
                description = updateRequest.description,
                price = updateRequest.price,
                typeCode = updateRequest.typeCode,
                concept = updateRequest.concept,
                originalProvideType = updateRequest.originalProvideType,
                partialOriginalCount = updateRequest.partialOriginalCount,
                shootingTime = updateRequest.shootingTime,
                shootingLocation = updateRequest.shootingLocation,
                numberOfCostumes = updateRequest.numberOfCostumes,
                seasonYear = updateRequest.seasonYear,
                seasonHalf = updateRequest.seasonHalf,
                partnerShops =
                listOf(
                    PartnerShopResponse(name = "Shop A Updated", link = "http://shopA-updated.com"),
                    PartnerShopResponse(name = "Shop B Updated", link = "http://shopB-updated.com"),
                ),
                detailedInfo = updateRequest.detailedInfo,
                warrantyInfo = updateRequest.warrantyInfo,
                contactInfo = updateRequest.contactInfo,
                createdAt = now,
                updatedAt = now,
                options =
                listOf(
                    ProductOptionResponse(
                        optionId = 1L,
                        productId = updateRequest.productId,
                        name = "Updated Option 1",
                        additionalPrice = 2000,
                        description = "Updated extra option details",
                        createdAt = now,
                        updatedAt = now,
                    ),
                ),
                images = listOf("updated_image1.jpg")
            )

        // given: 실제 이미지 파일 객체 생성
        // 컨트롤러에서 @RequestPart("images")로 받음
        val imageFile =
            MockMultipartFile(
                "images",
                "updated_image1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy updated image content".toByteArray(),
            )
        val imagesList: List<MultipartFile> = listOf(imageFile)

        // given: productUseCase.updateProduct를 실제 객체와 함께 stubbing
        given(productUseCase.updateProduct(updateRequest, imagesList)).willReturn(productResponse)

        // 수정 요청 JSON 파트 생성: objectMapper를 사용하여 updateRequest 객체를 JSON 문자열로 변환합니다.
        val requestJson = objectMapper.writeValueAsString(updateRequest)
        val requestPart =
            MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson.toByteArray(),
            )

        // 요청 빌더: multipart 요청 구성 (JSON 파트와 이미지 파일 파트를 함께 전송) 및 HTTP 메서드를 PUT으로 변경
        val requestBuilder =
            MockMvcRequestBuilders.multipart("/api/products/{id}", updateRequest.productId)
                .file(requestPart)
                .file(imageFile)
                .with { req ->
                    req.method = HttpMethod.PUT.toString()
                    req
                }
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON)

        // when/then: 요청 수행, 응답 검증 및 API 문서 생성
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "product-update",
                responseBody(
                    "success" type BOOLEAN means "응답 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "수정된 상품 데이터",
                    "data.productId" type NUMBER means "상품 ID",
                    "data.userId" type NUMBER means "사용자 ID",
                    "data.title" type STRING means "상품명",
                    "data.description" type STRING means "상품 설명",
                    "data.price" type NUMBER means "상품 가격",
                    "data.typeCode" type NUMBER means "상품 타입 코드",
                    "data.concept" type STRING means "콘셉트 (ELEGANT, VINTAGE, MODERN, CLASSIC, LUXURY)",
                    "data.originalProvideType" type STRING means "원본 제공 타입 (FULL, PARTIAL)",
                    "data.partialOriginalCount" type NUMBER means "제공할 원본 장수 (FULL이면 null 또는 0)",
                    "data.shootingTime" type STRING means "촬영 시간 (ISO 포맷)",
                    "data.shootingLocation" type STRING means "촬영 위치",
                    "data.numberOfCostumes" type NUMBER means "최대 의상 벌 수",
                    "data.seasonYear" type NUMBER means "시즌 연도",
                    "data.seasonHalf" type STRING means "상반기/하반기 (FIRST_HALF, SECOND_HALF)",
                    "data.partnerShops" type ARRAY means "협력업체 목록",
                    "data.partnerShops[].name" type STRING means "협력업체명",
                    "data.partnerShops[].link" type STRING means "협력업체 링크",
                    "data.detailedInfo" type STRING means "상세 정보",
                    "data.warrantyInfo" type STRING means "보증 정보",
                    "data.contactInfo" type STRING means "연락처",
                    "data.createdAt" type STRING means "생성 시간 (ISO 포맷)",
                    "data.updatedAt" type STRING means "수정 시간 (ISO 포맷)",
                    "data.options" type ARRAY means "상품 옵션 목록",
                    "data.options[].optionId" type NUMBER means "옵션 ID",
                    "data.options[].productId" type NUMBER means "옵션 관련 상품 ID",
                    "data.options[].name" type STRING means "옵션 이름",
                    "data.options[].additionalPrice" type NUMBER means "추가 가격",
                    "data.options[].description" type STRING means "옵션 설명",
                    "data.options[].createdAt" type STRING means "옵션 생성 시간 (ISO 포맷)",
                    "data.options[].updatedAt" type STRING means "옵션 수정 시간 (ISO 포맷)",
                    "data.images" type ARRAY means "상품 이미지 리스트",
                ),
            )
    }

    @Test
    fun `deleteProduct - 성공`() {
        val productId = 1L

        mockMvc.perform(
            RestDocumentationRequestBuilders.delete("/api/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isNoContent)
            .andDocument("product-delete")
    }

    @Test
    fun `getProduct - 성공`() {
        val now = LocalDateTime.now()
        val productId = 1L
        val productResponse =
            ProductResponse(
                productId = productId,
                userId = 1L,
                title = "Test Product",
                description = "A product for testing",
                price = 1000L,
                typeCode = 0,
                concept = ConceptType.MODERN,
                originalProvideType = OriginalProvideType.FULL,
                partialOriginalCount = null,
                shootingTime = now,
                shootingLocation = "Test Location",
                numberOfCostumes = 1,
                seasonYear = 2025,
                seasonHalf = SeasonHalf.FIRST_HALF,
                partnerShops = listOf(PartnerShopResponse("Shop1", "http://shop1.com")),
                detailedInfo = "Detailed info",
                warrantyInfo = "Warranty info",
                contactInfo = "Contact info",
                createdAt = now,
                updatedAt = now,
                options =
                listOf(
                    ProductOptionResponse(
                        optionId = 1L,
                        productId = productId,
                        name = "Option1",
                        additionalPrice = 100L,
                        description = "Option description",
                        createdAt = now,
                        updatedAt = now,
                    ),
                ),
                images = listOf("image1.png", "image2.png"),
            )

        given(productUseCase.getProductById(productId)).willReturn(productResponse)

        mockMvc.perform(
            RestDocumentationRequestBuilders.get("/api/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andDocument(
                "product-get",
                responseBody(
                    "data" type OBJECT means "조회된 상품 데이터",
                    "data.productId" type NUMBER means "상품 ID",
                    "data.userId" type NUMBER means "사용자 ID",
                    "data.title" type STRING means "상품명",
                    "data.description" type STRING means "상품 설명",
                    "data.price" type NUMBER means "상품 가격",
                    "data.typeCode" type NUMBER means "상품 타입 코드",
                    "data.concept" type STRING means "콘셉트 (ELEGANT, VINTAGE, MODERN, CLASSIC, LUXURY)",
                    "data.originalProvideType" type STRING means "원본 제공 타입 (FULL, PARTIAL)",
                    "data.partialOriginalCount" type NUMBER means "제공할 원본 장수 (FULL이면 null 또는 0)",
                    "data.shootingTime" type STRING means "촬영 시간 (ISO 포맷)",
                    "data.shootingLocation" type STRING means "촬영 위치",
                    "data.numberOfCostumes" type NUMBER means "최대 의상 벌 수",
                    "data.seasonYear" type NUMBER means "시즌 연도",
                    "data.seasonHalf" type STRING means "상반기/하반기 (FIRST_HALF, SECOND_HALF)",
                    "data.partnerShops" type ARRAY means "협력업체 목록",
                    "data.partnerShops[].name" type STRING means "협력업체 이름",
                    "data.partnerShops[].link" type STRING means "협력업체 링크",
                    "data.detailedInfo" type STRING means "상세 정보",
                    "data.warrantyInfo" type STRING means "보증 정보",
                    "data.contactInfo" type STRING means "연락처 정보",
                    "data.createdAt" type STRING means "생성 시간 (ISO 포맷)",
                    "data.updatedAt" type STRING means "수정 시간 (ISO 포맷)",
                    "data.options" type ARRAY means "상품 옵션 목록",
                    "data.options[].optionId" type NUMBER means "옵션 ID",
                    "data.options[].productId" type NUMBER means "옵션 관련 상품 ID",
                    "data.options[].name" type STRING means "옵션 이름",
                    "data.options[].additionalPrice" type NUMBER means "추가 가격",
                    "data.options[].description" type STRING means "옵션 설명",
                    "data.options[].createdAt" type STRING means "옵션 생성 시간 (ISO 포맷)",
                    "data.options[].updatedAt" type STRING means "옵션 수정 시간 (ISO 포맷)",
                    "data.images" type ARRAY means "상품 이미지 리스트",
                    "success" type BOOLEAN means "성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                ),
            )
    }

    @Test
    fun `getMainPageProducts - 성공`() {
        val now = LocalDateTime.now()
        val productResponse =
            ProductResponse(
                productId = 1L,
                userId = 1L,
                title = "Test Product",
                description = "A product for testing",
                price = 1000L,
                typeCode = 0,
                concept = ConceptType.MODERN,
                originalProvideType = OriginalProvideType.FULL,
                partialOriginalCount = null,
                shootingTime = now,
                shootingLocation = "Test Location",
                numberOfCostumes = 1,
                seasonYear = 2025,
                seasonHalf = SeasonHalf.FIRST_HALF,
                partnerShops = listOf(PartnerShopResponse("Shop1", "http://shop1.com")),
                detailedInfo = "Detailed info",
                warrantyInfo = "Warranty info",
                contactInfo = "Contact info",
                createdAt = now,
                updatedAt = now,
                options =
                listOf(
                    ProductOptionResponse(
                        optionId = 1L,
                        productId = 1L,
                        name = "Option1",
                        additionalPrice = 100L,
                        description = "Option description",
                        createdAt = now,
                        updatedAt = now,
                    ),
                ),
                images = listOf("image1.png", "image2.png"),
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

        mockMvc.perform(
            RestDocumentationRequestBuilders.get("/api/products/main")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andDocument(
                "product-main",
                responseBody(
                    "data.content" type ARRAY means "상품 목록",
                    "data.content[].productId" type NUMBER means "상품 ID",
                    "data.content[].userId" type NUMBER means "사용자 ID",
                    "data.content[].title" type STRING means "상품명",
                    "data.content[].description" type STRING means "상품 설명",
                    "data.content[].price" type NUMBER means "상품 가격",
                    "data.content[].typeCode" type NUMBER means "상품 타입 코드",
                    "data.content[].concept" type STRING means "콘셉트 (ELEGANT, VINTAGE, MODERN, CLASSIC, LUXURY)",
                    "data.content[].originalProvideType" type STRING means "원본 제공 타입 (FULL, PARTIAL)",
                    "data.content[].partialOriginalCount" type NUMBER means "제공할 원본 장수 (FULL이면 null 또는 0)",
                    "data.content[].shootingTime" type STRING means "촬영 시간 (ISO 포맷)",
                    "data.content[].shootingLocation" type STRING means "촬영 위치",
                    "data.content[].numberOfCostumes" type NUMBER means "최대 의상 벌 수",
                    "data.content[].seasonYear" type NUMBER means "시즌 연도",
                    "data.content[].seasonHalf" type STRING means "상반기/하반기 (FIRST_HALF, SECOND_HALF)",
                    "data.content[].partnerShops" type ARRAY means "협력업체 목록",
                    "data.content[].partnerShops[].name" type STRING means "협력업체 이름",
                    "data.content[].partnerShops[].link" type STRING means "협력업체 링크",
                    "data.content[].detailedInfo" type STRING means "상세 정보",
                    "data.content[].warrantyInfo" type STRING means "보증 정보",
                    "data.content[].contactInfo" type STRING means "연락처 정보",
                    "data.content[].createdAt" type STRING means "생성 시간 (ISO 포맷)",
                    "data.content[].updatedAt" type STRING means "수정 시간 (ISO 포맷)",
                    "data.content[].options" type ARRAY means "상품 옵션 목록",
                    "data.content[].options[].optionId" type NUMBER means "옵션 ID",
                    "data.content[].options[].productId" type NUMBER means "옵션 관련 상품 ID",
                    "data.content[].options[].name" type STRING means "옵션 이름",
                    "data.content[].options[].additionalPrice" type NUMBER means "추가 가격",
                    "data.content[].options[].description" type STRING means "옵션 설명",
                    "data.content[].options[].createdAt" type STRING means "옵션 생성 시간 (ISO 포맷)",
                    "data.content[].options[].updatedAt" type STRING means "옵션 수정 시간 (ISO 포맷)",
                    "data.content[].images" type ARRAY means "상품 이미지 리스트",
                    "data.page" type NUMBER means "현재 페이지",
                    "data.size" type NUMBER means "페이지 크기",
                    "data.totalElements" type NUMBER means "전체 상품 수",
                    "data.totalPages" type NUMBER means "전체 페이지 수",
                    "success" type BOOLEAN means "성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                ),
            )
    }
}
