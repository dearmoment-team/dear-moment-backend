package kr.kro.dearmoment.product.adapter.input.web.search

import andDocument
import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.common.dto.ResponseWrapper
import kr.kro.dearmoment.common.restdocs.ARRAY
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.product.adapter.input.web.ProductRestAdapter
import kr.kro.dearmoment.product.application.dto.response.ImageResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.usecase.create.CreateProductUseCase
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductOptionUseCase
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
class SearchProductRestAdapterTest {
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

    @MockitoBean
    lateinit var deleteProductOptionUseCase: DeleteProductOptionUseCase

    @Test
    fun `상품 검색 API 테스트 - 정상 검색`() {
        // given
        val product1 =
            ProductResponse(
                productId = 1L,
                userId = 10L,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "My Wedding Snap",
                description = "Desc1",
                availableSeasons = listOf("YEAR_2025_FIRST_HALF"),
                cameraTypes = listOf("FILM"),
                retouchStyles = listOf("NATURAL"),
                mainImage = ImageResponse(imageId = 1L, url = "http://image-server.com/main1.jpg"),
                subImages =
                    listOf(
                        ImageResponse(imageId = 2L, url = "http://image-server.com/sub1.jpg"),
                    ),
                additionalImages = emptyList(),
                detailedInfo = "Info1",
                contactInfo = "Contact1",
                createdAt = null,
                updatedAt = null,
                options = emptyList(),
            )

        val product2 =
            ProductResponse(
                productId = 2L,
                userId = 11L,
                productType = "WEDDING_SNAP",
                shootingPlace = "SEOUL",
                title = "Another Snap",
                description = "Desc2",
                availableSeasons = listOf("YEAR_2025_SECOND_HALF"),
                cameraTypes = listOf("DIGITAL"),
                retouchStyles = listOf("CALM"),
                mainImage = ImageResponse(imageId = 3L, url = "http://image-server.com/main2.jpg"),
                subImages =
                    listOf(
                        ImageResponse(imageId = 4L, url = "http://image-server.com/sub2.jpg"),
                    ),
                additionalImages = emptyList(),
                detailedInfo = "Info2",
                contactInfo = "Contact2",
                createdAt = null,
                updatedAt = null,
                options = emptyList(),
            )

        val pagedResult =
            PagedResponse(
                content = listOf(product1, product2),
                page = 0,
                size = 10,
                totalElements = 2,
                totalPages = 1,
            )

        // Mock UseCase 결과
        given(
            productSearchUseCase.searchProducts(
                title = "Snap",
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                sortBy = "created-desc",
                page = 0,
                size = 10,
            ),
        ).willReturn(pagedResult)

        // when
        val requestBuilder =
            RestDocumentationRequestBuilders
                .get("/api/products/search")
                .param("title", "Snap")
                .param("productType", "WEDDING_SNAP")
                .param("shootingPlace", "JEJU")
                .param("sortBy", "created-desc")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON)

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "search-products",
                responseBody(
                    "success" type BOOLEAN means "요청 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "페이징 된 상품 목록 데이터",
                    "data.content" type ARRAY means "상품 목록 Content",
                    "data.content[].productId" type NUMBER means "상품 ID",
                    "data.content[].userId" type NUMBER means "사용자 ID",
                    "data.content[].productType" type STRING means "상품 유형",
                    "data.content[].shootingPlace" type STRING means "촬영 장소",
                    "data.content[].title" type STRING means "상품명",
                    "data.content[].description" type STRING means "상품 설명",
                    "data.content[].availableSeasons" type ARRAY means "촬영 가능 시기 목록",
                    "data.content[].cameraTypes" type ARRAY means "카메라 종류 목록",
                    "data.content[].retouchStyles" type ARRAY means "보정 스타일 목록",
                    "data.content[].mainImage" type OBJECT means "대표 이미지",
                    "data.content[].mainImage.imageId" type NUMBER means "대표 이미지 ID",
                    "data.content[].mainImage.url" type STRING means "대표 이미지 URL",
                    "data.content[].subImages" type ARRAY means "서브 이미지 목록",
                    "data.content[].subImages[].imageId" type NUMBER means "서브 이미지 ID",
                    "data.content[].subImages[].url" type STRING means "서브 이미지 URL",
                    "data.content[].additionalImages" type ARRAY means "추가 이미지 목록",
                    "data.content[].additionalImages[].imageId" type NUMBER means "추가 이미지 ID",
                    "data.content[].additionalImages[].url" type STRING means "추가 이미지 URL",
                    "data.content[].detailedInfo" type STRING means "상세 정보",
                    "data.content[].contactInfo" type STRING means "연락처",
                    "data.content[].createdAt" type OBJECT means "생성 일자",
                    "data.content[].updatedAt" type OBJECT means "수정 일자",
                    "data.content[].options" type ARRAY means "옵션 목록",
                    // 페이징 정보
                    "data.page" type NUMBER means "현재 페이지 번호",
                    "data.size" type NUMBER means "페이지 크기",
                    "data.totalElements" type NUMBER means "전체 원소 개수",
                    "data.totalPages" type NUMBER means "전체 페이지 개수",
                ),
            )
    }

    @Test
    fun `메인 페이지 상품 조회 API 테스트 - 정상 조회`() {
        // given
        val product1 =
            ProductResponse(
                productId = 10L,
                userId = 5L,
                productType = "WEDDING_SNAP",
                shootingPlace = "BUSAN",
                title = "Busan Snap",
                description = "Desc",
                availableSeasons = listOf("YEAR_2025_FIRST_HALF"),
                cameraTypes = listOf("DIGITAL"),
                retouchStyles = listOf("NATURAL"),
                mainImage = ImageResponse(imageId = 11L, url = "http://image-server.com/main.jpg"),
                subImages =
                    listOf(
                        ImageResponse(imageId = 12L, url = "http://image-server.com/sub1.jpg"),
                    ),
                additionalImages = listOf(),
                detailedInfo = "Some info",
                contactInfo = "contact@example.com",
                createdAt = null,
                updatedAt = null,
                options = emptyList(),
            )
        val pagedResult =
            PagedResponse(
                content = listOf(product1),
                page = 0,
                size = 10,
                totalElements = 1,
                totalPages = 1,
            )

        given(productSearchUseCase.getMainPageProducts(page = 0, size = 10))
            .willReturn(pagedResult)

        // when
        val requestBuilder =
            RestDocumentationRequestBuilders
                .get("/api/products/main")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON)

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk)
            .andDocument(
                "get-main-page-products",
                responseBody(
                    "success" type BOOLEAN means "요청 성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                    "data" type OBJECT means "페이징 된 상품 목록",
                    "data.content" type ARRAY means "상품 목록 Content",
                    "data.content[].productId" type NUMBER means "상품 ID",
                    "data.content[].userId" type NUMBER means "사용자 ID",
                    "data.content[].productType" type STRING means "상품 유형",
                    "data.content[].shootingPlace" type STRING means "촬영 장소",
                    "data.content[].title" type STRING means "상품명",
                    "data.content[].description" type STRING means "상품 설명",
                    "data.content[].availableSeasons" type ARRAY means "촬영 가능 시기 목록",
                    "data.content[].cameraTypes" type ARRAY means "카메라 종류 목록",
                    "data.content[].retouchStyles" type ARRAY means "보정 스타일 목록",
                    "data.content[].mainImage" type OBJECT means "대표 이미지",
                    "data.content[].mainImage.imageId" type NUMBER means "대표 이미지 ID",
                    "data.content[].mainImage.url" type STRING means "대표 이미지 URL",
                    "data.content[].subImages" type ARRAY means "서브 이미지 목록",
                    "data.content[].subImages[].imageId" type NUMBER means "서브 이미지 ID",
                    "data.content[].subImages[].url" type STRING means "서브 이미지 URL",
                    "data.content[].additionalImages" type ARRAY means "추가 이미지 목록",
                    "data.content[].additionalImages[].imageId" type NUMBER means "추가 이미지 ID",
                    "data.content[].additionalImages[].url" type STRING means "추가 이미지 URL",
                    "data.content[].detailedInfo" type STRING means "상세 정보",
                    "data.content[].contactInfo" type STRING means "연락처",
                    "data.content[].createdAt" type OBJECT means "생성 일자",
                    "data.content[].updatedAt" type OBJECT means "수정 일자",
                    "data.content[].options" type ARRAY means "옵션 목록",
                    // 페이징 정보
                    "data.page" type NUMBER means "현재 페이지 번호",
                    "data.size" type NUMBER means "페이지 크기",
                    "data.totalElements" type NUMBER means "전체 원소 개수",
                    "data.totalPages" type NUMBER means "전체 페이지 개수",
                ),
            )
    }
}
