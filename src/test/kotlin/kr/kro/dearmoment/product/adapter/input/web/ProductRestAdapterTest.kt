package kr.kro.dearmoment.product.adapter.input.web

import andDocument
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
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.usecase.ProductUseCase
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductUseCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
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

    @Autowired
    lateinit var deleteProductUseCase: DeleteProductUseCase

    @Test
    fun `상품 생성 API 테스트`() {
        // given
        val mainImage =
            MockMultipartFile(
                "mainImageFile",
                "mainImage.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "main image content".toByteArray(),
            )
        val subImage1 =
            MockMultipartFile(
                "subImageFiles",
                "subImage1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "sub image 1".toByteArray(),
            )
        val subImage2 =
            MockMultipartFile(
                "subImageFiles",
                "subImage2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "sub image 2".toByteArray(),
            )
        val subImage3 =
            MockMultipartFile(
                "subImageFiles",
                "subImage3.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "sub image 3".toByteArray(),
            )
        val subImage4 =
            MockMultipartFile(
                "subImageFiles",
                "subImage4.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "sub image 4".toByteArray(),
            )

        val request =
            CreateProductRequest(
                userId = 1L,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "New Product",
                description = "Product description",
                availableSeasons = listOf("YEAR_2025_FIRST_HALF"),
                cameraTypes = listOf("DIGITAL"),
                retouchStyles = listOf("MODERN"),
                mainImageFile = mainImage,
                subImageFiles = listOf(subImage1, subImage2, subImage3, subImage4),
                additionalImageFiles = emptyList(),
                detailedInfo = "Detailed product information",
                contactInfo = "contact@example.com",
                options =
                listOf(
                    CreateProductOptionRequest(
                        name = "Option 1",
                        optionType = "SINGLE",
                        discountAvailable = false,
                        originalPrice = 10000,
                        discountPrice = 1000,
                        description = "Extra option",
                        costumeCount = 1,
                        shootingLocationCount = 1,
                        shootingHours = 1,
                        shootingMinutes = 30,
                        retouchedCount = 1,
                        originalProvided = true,
                        partnerShops =
                        listOf(
                            CreatePartnerShopRequest(
                                category = "DRESS",
                                name = "Shop A",
                                link = "http://shopA.com",
                            ),
                            CreatePartnerShopRequest(
                                category = "DRESS",
                                name = "Shop B",
                                link = "http://shopB.com",
                            ),
                        ),
                    ),
                ),
            )

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
                options = listOf(),
            )

        // mock 결과
        given(productUseCase.saveProduct(request)).willReturn(productResponse)

        // when
        val requestBuilder =
            multipart("/api/products")
                .file(mainImage)
                .file(subImage1)
                .file(subImage2)
                .file(subImage3)
                .file(subImage4)
                .param("userId", "1")
                .param("productType", "WEDDING_SNAP")
                .param("shootingPlace", "JEJU")
                .param("title", "New Product")
                .param("description", "Product description")
                .param("availableSeasons", "YEAR_2025_FIRST_HALF")
                .param("cameraTypes", "DIGITAL")
                .param("retouchStyles", "MODERN")
                .param("detailedInfo", "Detailed product information")
                .param("contactInfo", "contact@example.com")
                .characterEncoding("UTF-8") // 인코딩 설정 추가
                .contentType(MediaType.MULTIPART_FORM_DATA)

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isCreated)
            .andDocument(
                "create-product",
                responseBody(
                    "success" type BOOLEAN means "요청 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "실제 상품 데이터",
                    // 이제 data 내부 필드를 문서화 (data.productId, data.userId, ...)
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
                    "data.subImages" type ARRAY means "서브 이미지 URL 리스트",
                    "data.additionalImages" type ARRAY means "추가 이미지 URL 리스트",
                    "data.detailedInfo" type STRING means "상세 정보",
                    "data.contactInfo" type STRING means "연락처",
                    "data.createdAt" type OBJECT means "생성 시간",
                    "data.updatedAt" type OBJECT means "수정 시간",
                    "data.options" type ARRAY means "옵션 목록(생성된 옵션이 없어서 빈 배열)",
                    // 옵션 목록 상세 필드는 옵션이 있으면 추가로 문서화
                ),
            )
    }

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
        given(productUseCase.getProductById(1L)).willReturn(productResponse)

        // when
        val requestBuilder =
            RestDocumentationRequestBuilders
                .get("/api/products/{id}", 1L)

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
        given(productUseCase.getProductById(999L))
            .willThrow(IllegalArgumentException("Product with ID 999 not found."))

        // when
        val requestBuilder =
            RestDocumentationRequestBuilders
                .get("/api/products/{id}", 999L)

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument(
                "get-product-not-found",
                // 400이므로 wrapper가 적용되지 않는다고 가정 => 단순 message만 문서화
                responseBody(
                    "message" type STRING means "에러 메시지",
                ),
            )
    }

    @Test
    fun `상품 수정 API 테스트 - 정상 수정 (옵션 삭제 포함)`() {
        // given
        val updateRequest =
            UpdateProductRequest(
                productId = 1L,
                userId = 1L,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "Updated Product",
                description = "Updated description",
                availableSeasons = listOf("YEAR_2025_FIRST_HALF"),
                cameraTypes = listOf("DIGITAL"),
                retouchStyles = listOf("MODERN"),
                mainImageFile = null,
                subImagesFinal = emptyList(),
                additionalImagesFinal = emptyList(),
                detailedInfo = "Updated detailed information",
                contactInfo = "updated@example.com",
                options =
                listOf(
                    UpdateProductOptionRequest(
                        optionId = 1L,
                        name = "Option 1 Updated",
                        optionType = "SINGLE",
                        discountAvailable = false,
                        originalPrice = 10000,
                        discountPrice = 900,
                        description = "Extra option updated",
                        costumeCount = 1,
                        shootingLocationCount = 1,
                        shootingHours = 1,
                        shootingMinutes = 30,
                        retouchedCount = 1,
                        originalProvided = true,
                        partnerShops = emptyList(),
                    ),
                ),
            )

        val updatedProductResponse =
            ProductResponse(
                productId = 1L,
                userId = 1L,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "Updated Product",
                description = "Updated description",
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
                detailedInfo = "Updated detailed information",
                contactInfo = "updated@example.com",
                createdAt = null,
                updatedAt = null,
                options =
                listOf(
                    ProductOptionResponse(
                        optionId = 1L,
                        productId = 1L,
                        name = "Option 1 Updated",
                        optionType = "SINGLE",
                        discountAvailable = false,
                        originalPrice = 10000,
                        discountPrice = 900,
                        description = "Extra option updated",
                        costumeCount = 1,
                        shootingLocationCount = 1,
                        shootingHours = 1,
                        shootingMinutes = 30,
                        retouchedCount = 1,
                        partnerShops = emptyList(),
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
                .contentType(MediaType.MULTIPART_FORM_DATA)

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "update-product",
                responseBody(
                    "success" type BOOLEAN means "요청 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "실제 수정된 상품 데이터",
                    // data.* 문서화
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
                    "data.subImages" type ARRAY means "서브 이미지 URL 리스트",
                    "data.additionalImages" type ARRAY means "추가 이미지 URL 리스트",
                    "data.detailedInfo" type STRING means "상세 정보",
                    "data.contactInfo" type STRING means "연락처",
                    "data.createdAt" type OBJECT means "생성 시간",
                    "data.updatedAt" type OBJECT means "수정 시간",
                    "data.options" type ARRAY means "옵션 목록",
                    "data.options[].optionId" type NUMBER means "옵션 ID",
                    "data.options[].productId" type NUMBER means "상품 ID",
                    "data.options[].name" type STRING means "옵션명",
                    "data.options[].optionType" type STRING means "옵션 타입",
                    "data.options[].discountAvailable" type BOOLEAN means "할인 여부",
                    "data.options[].originalPrice" type NUMBER means "원 가격",
                    "data.options[].discountPrice" type NUMBER means "할인 가격",
                    "data.options[].description" type STRING means "옵션 설명",
                    "data.options[].costumeCount" type NUMBER means "의상 수",
                    "data.options[].shootingLocationCount" type NUMBER means "촬영 장소 수",
                    "data.options[].shootingHours" type NUMBER means "촬영 시간(시)",
                    "data.options[].shootingMinutes" type NUMBER means "촬영 시간(분)",
                    "data.options[].retouchedCount" type NUMBER means "보정본 수",
                    "data.options[].partnerShops" type ARRAY means "파트너샵 목록",
                ),
            )
    }

    @Test
    fun `상품 수정 API 테스트 - 존재하지 않는 상품`() {
        // given
        val updateRequest =
            UpdateProductRequest(
                productId = 999L,
                userId = 1L,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "Updated Product",
                description = "Updated description",
                availableSeasons = listOf("YEAR_2025_FIRST_HALF"),
                cameraTypes = listOf("DIGITAL"),
                retouchStyles = listOf("MODERN"),
                mainImageFile = null,
                subImagesFinal = emptyList(),
                additionalImagesFinal = emptyList(),
                detailedInfo = "Updated detailed information",
                contactInfo = "updated@example.com",
                options = emptyList(),
            )

        given(productUseCase.updateProduct(updateRequest))
            .willThrow(IllegalArgumentException("Product not found: 999"))

        // when
        val requestBuilder =
            RestDocumentationRequestBuilders
                .put("/api/products/{id}", 999L)
                .content(updateRequest.toJsonString())
                .contentType(MediaType.MULTIPART_FORM_DATA)

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument(
                "update-product-not-found",
                // wrapper 적용 안됨 → 단순 { "message": ... } 형태
                responseBody(
                    "message" type STRING means "에러 메시지",
                ),
            )
    }

    @Test
    fun `상품 삭제 API 테스트 - 정상 삭제`() {
        // given
        org.mockito.Mockito.doNothing().`when`(deleteProductUseCase).deleteProduct(1L)

        val requestBuilder =
            RestDocumentationRequestBuilders
                .delete("/api/products/{id}", 1L)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isNoContent)
            .andDocument("delete-product")
    }

    @Test
    fun `상품 삭제 API 테스트 - 존재하지 않는 상품`() {
        // given
        org.mockito.Mockito.doThrow(IllegalArgumentException("The product to delete does not exist: 999."))
            .`when`(deleteProductUseCase).deleteProduct(999L)

        val requestBuilder =
            RestDocumentationRequestBuilders
                .delete("/api/products/{id}", 999L)

        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument(
                "delete-product-not-found",
                responseBody(
                    "message" type STRING means "에러 메시지",
                ),
            )
    }
}
