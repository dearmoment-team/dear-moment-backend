package kr.kro.dearmoment.product.adapter.input.web.create

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
import kr.kro.dearmoment.product.adapter.input.web.ProductRestAdapterTestConfig
import kr.kro.dearmoment.product.application.dto.request.CreatePartnerShopRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PartnerShopResponse
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.usecase.create.CreateProductUseCase
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(RestDocumentationExtension::class)
@WebMvcTest(ProductRestAdapter::class)
@Import(ProductRestAdapterTestConfig::class, ResponseWrapper::class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
class CreateProductRestAdapterTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var createProductUseCase: CreateProductUseCase

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

        // 요청 객체 세팅
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
                            CreatePartnerShopRequest("DRESS", "Shop A", "http://shopA.com"),
                            CreatePartnerShopRequest("DRESS", "Shop B", "http://shopB.com"),
                        ),
                    ),
                    CreateProductOptionRequest(
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
                        originalProvided = false,
                        partnerShops =
                        listOf(
                            CreatePartnerShopRequest("DRESS", "Shop C", "http://shopC.com"),
                        ),
                    ),
                ),
            )

        // 응답 객체 세팅
        val productOptionResponse1 =
            ProductOptionResponse(
                optionId = 1L,
                productId = 1L,
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
                partnerShops =
                listOf(
                    PartnerShopResponse("DRESS", "Shop A", "http://shopA.com"),
                    PartnerShopResponse("DRESS", "Shop B", "http://shopB.com"),
                ),
                createdAt = null,
                updatedAt = null,
            )

        val productOptionResponse2 =
            ProductOptionResponse(
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
                partnerShops =
                listOf(
                    PartnerShopResponse("DRESS", "Shop C", "http://shopC.com"),
                ),
                createdAt = null,
                updatedAt = null,
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
                options = listOf(productOptionResponse1, productOptionResponse2),
            )

        // createProductUseCase mock 동작 설정
        given(createProductUseCase.saveProduct(request)).willReturn(productResponse)

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
                .characterEncoding("UTF-8")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON)

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isCreated)
            .andDocument(
                "create-product",
                responseBody(
                    "success" type BOOLEAN means "요청 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "실제 상품 데이터",
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
                    // 옵션 상세 필드 추가
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
                ),
            )
    }
}
