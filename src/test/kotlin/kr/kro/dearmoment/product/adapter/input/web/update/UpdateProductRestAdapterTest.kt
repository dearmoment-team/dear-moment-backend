package kr.kro.dearmoment.product.adapter.input.web.update

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
import kr.kro.dearmoment.product.application.dto.request.AdditionalImageFinalRequest
import kr.kro.dearmoment.product.application.dto.request.SubImageFinalRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateAdditionalImageAction
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateSubImageAction
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.usecase.update.UpdateProductUseCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(RestDocumentationExtension::class)
@WebMvcTest(ProductRestAdapter::class)
@Import(ProductRestAdapterTestConfig::class, ResponseWrapper::class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
class UpdateProductRestAdapterTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var updateProductUseCase: UpdateProductUseCase

    @Test
    fun `상품 업데이트 API 테스트 - 정상 케이스`() {
        // given
        // 새로 업로드할 메인 이미지
        val mainImageFile =
            MockMultipartFile(
                "mainImageFile",
                "updated_main.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "updated main image content".toByteArray(),
            )

        // 서브/추가 이미지 처리를 위한 MockMultipartFile 예시
        val subImageFile1 =
            MockMultipartFile(
                "subImagesFinal[2].file",
                "sub3.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "new sub image content #3".toByteArray(),
            )
        val subImageFile2 =
            MockMultipartFile(
                "subImagesFinal[3].file",
                "sub4.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "new sub image content #4".toByteArray(),
            )
        val additionalImageFile =
            MockMultipartFile(
                "additionalImagesFinal[1].file",
                "add2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "new additional image content".toByteArray(),
            )

        // 실제 UpdateProductRequest를 구성하기 위해, 폼 필드로 전송할 값들.
        // (RestDocs 시나리오에서는 Multipart 전송 시 RequestParam, 혹은 JSON 필드로 넘길 수 있음)
        // 여기서는 param() 호출 방식으로 예시를 들지만, 프로젝트 설정에 따라 구조를 맞추세요.
        val request =
            UpdateProductRequest(
                productId = 1L,
                userId = 10L,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "Updated Product Title",
                description = "Updated Description",
                availableSeasons = listOf("YEAR_2025_SECOND_HALF"),
                cameraTypes = listOf("DIGITAL"),
                retouchStyles = listOf("CALM"),
                mainImageFile = mainImageFile,
                subImagesFinal =
                listOf(
                    SubImageFinalRequest(UpdateSubImageAction.KEEP, 200L, null),
                    SubImageFinalRequest(UpdateSubImageAction.DELETE, 201L, null),
                    SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, subImageFile1),
                    SubImageFinalRequest(UpdateSubImageAction.UPLOAD, null, subImageFile2),
                ),
                additionalImagesFinal =
                listOf(
                    AdditionalImageFinalRequest(UpdateAdditionalImageAction.DELETE, 300L, null),
                    AdditionalImageFinalRequest(UpdateAdditionalImageAction.UPLOAD, null, additionalImageFile),
                ),
                detailedInfo = "Updated Detailed Info",
                contactInfo = "updated-contact@example.com",
                options =
                listOf(
                    UpdateProductOptionRequest(
                        optionId = 1L,
                        name = "New Option 1",
                        optionType = "SINGLE",
                        discountAvailable = true,
                        originalPrice = 10000,
                        discountPrice = 7000,
                        description = "Updated Option Desc",
                        costumeCount = 1,
                        shootingLocationCount = 1,
                        shootingHours = 2,
                        shootingMinutes = 0,
                        retouchedCount = 3,
                        originalProvided = true,
                        partnerShops = emptyList(),
                    ),
                ),
            )

        // UseCase에서 업데이트 후 반환할 더미 응답 객체
        val updatedResponse =
            ProductResponse(
                productId = 1L,
                userId = 10L,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "Updated Product Title",
                description = "Updated Description",
                availableSeasons = listOf("YEAR_2025_SECOND_HALF"),
                cameraTypes = listOf("DIGITAL"),
                retouchStyles = listOf("CALM"),
                mainImage = "http://image-server.com/updated_main.jpg",
                subImages =
                listOf(
                    "http://image-server.com/subImage1_KEPT.jpg",
                    "http://image-server.com/subImage3_UPLOADED.jpg",
                    "http://image-server.com/subImage4_UPLOADED.jpg",
                ),
                additionalImages = listOf("http://image-server.com/additionalImage2_UPLOADED.jpg"),
                detailedInfo = "Updated Detailed Info",
                contactInfo = "updated-contact@example.com",
                createdAt = null,
                updatedAt = null,
                options =
                listOf(
                    ProductOptionResponse(
                        optionId = 1L,
                        productId = 1L,
                        name = "New Option 1",
                        optionType = "SINGLE",
                        discountAvailable = true,
                        originalPrice = 10000,
                        discountPrice = 7000,
                        description = "Updated Option Desc",
                        costumeCount = 1,
                        shootingLocationCount = 1,
                        shootingHours = 2,
                        shootingMinutes = 0,
                        retouchedCount = 3,
                        partnerShops = emptyList(),
                        createdAt = null,
                        updatedAt = null,
                    ),
                ),
            )

        // UpdateProductUseCase 모킹
        given(updateProductUseCase.updateProduct(request)).willReturn(updatedResponse)

        // 실제 multipart 요청 생성
        val requestBuilder =
            MockMvcRequestBuilders
                .multipart("/api/products/{id}", 1L)
                // 파일 attach
                .file(mainImageFile)
                .file(subImageFile1)
                .file(subImageFile2)
                .file(additionalImageFile)
                // multipart지만 PUT 메서드로 동작하도록 설정
                .with { req ->
                    req.method = HttpMethod.PUT.toString()
                    req
                }
                // 필요 파라미터들
                .param("productId", request.productId.toString())
                .param("userId", request.userId.toString())
                .param("productType", request.productType)
                .param("shootingPlace", request.shootingPlace)
                .param("title", request.title)
                .param("description", request.description)
                .param("availableSeasons", "YEAR_2025_SECOND_HALF")
                .param("cameraTypes", "DIGITAL")
                .param("retouchStyles", "CALM")
                .param("detailedInfo", request.detailedInfo)
                .param("contactInfo", request.contactInfo)
                // 옵션도 문자열 파라미터로 전달 (JSON 직렬화 or 다른 방식 사용 가능)
                .param("options[0].optionId", "1")
                .param("options[0].name", "New Option 1")
                .param("options[0].optionType", "SINGLE")
                .param("options[0].discountAvailable", "true")
                .param("options[0].originalPrice", "10000")
                .param("options[0].discountPrice", "7000")
                .param("options[0].description", "Updated Option Desc")
                .param("options[0].costumeCount", "1")
                .param("options[0].shootingLocationCount", "1")
                .param("options[0].shootingHours", "2")
                .param("options[0].shootingMinutes", "0")
                .param("options[0].retouchedCount", "3")
                .param("options[0].originalProvided", "true")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")

        // when, then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "update-product",
                responseBody(
                    "success" type BOOLEAN means "요청 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "수정된 상품 데이터",
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
                    "data.createdAt" type OBJECT means "생성 일자",
                    "data.updatedAt" type OBJECT means "수정 일자",
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
                    "data.options[].createdAt" type OBJECT means "옵션 생성 시간",
                    "data.options[].updatedAt" type OBJECT means "옵션 수정 시간",
                ),
            )
    }

    @Test
    fun `상품 업데이트 API 테스트 - 존재하지 않는 상품`() {
        // given
        val invalidRequest =
            UpdateProductRequest(
                productId = 9999L,
                userId = 10L,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "Invalid Product Title",
                description = "Invalid Description",
                availableSeasons = emptyList(),
                cameraTypes = emptyList(),
                retouchStyles = emptyList(),
                mainImageFile = null,
                subImagesFinal = emptyList(),
                additionalImagesFinal = emptyList(),
                detailedInfo = "",
                contactInfo = "",
                options = emptyList(),
            )

        doThrow(IllegalArgumentException("상품을 찾을 수 없습니다. ID: 9999"))
            .`when`(updateProductUseCase).updateProduct(invalidRequest)

        val requestBuilder =
            MockMvcRequestBuilders
                .multipart("/api/products/{id}", 9999L)
                // PUT 메서드 지정
                .with { req ->
                    req.method = "PUT"
                    req
                }
                .param("productId", "9999")
                .param("userId", "10")
                .param("productType", "WEDDING_SNAP")
                .param("shootingPlace", "JEJU")
                .param("title", "Invalid Product Title")
                .param("description", "Invalid Description")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON)

        // when, then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest)
            .andDocument(
                "update-product-not-found",
                responseBody(
                    "message" type STRING means "에러 메시지",
                ),
            )
    }
}
