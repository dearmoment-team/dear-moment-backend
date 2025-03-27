package kr.kro.dearmoment.product.adapter.input.web.search

import andDocument
import io.mockk.every
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.common.restdocs.ARRAY
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.product.application.dto.response.ImageResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class SearchProductRestAdapterTest : RestApiTestBase() {
    @Test
    fun `상품 검색 API 테스트 - 정상 검색`() {
        // given
        val dummyUserId1 = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val dummyUserId2 = UUID.fromString("22222222-2222-2222-2222-222222222222")
        val product1 =
            ProductResponse(
                productId = 1L,
                userId = dummyUserId1,
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                title = "My Wedding Snap",
                description = "Desc1",
                availableSeasons = listOf("YEAR_2025_FIRST_HALF"),
                cameraTypes = listOf("FILM"),
                retouchStyles = listOf("NATURAL"),
                mainImage = ImageResponse(imageId = 1L, url = "http://image-server.com/main1.jpg"),
                subImages = listOf(ImageResponse(imageId = 2L, url = "http://image-server.com/sub1.jpg")),
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
                userId = dummyUserId2,
                productType = "WEDDING_SNAP",
                shootingPlace = "SEOUL",
                title = "Another Snap",
                description = "Desc2",
                availableSeasons = listOf("YEAR_2025_SECOND_HALF"),
                cameraTypes = listOf("DIGITAL"),
                retouchStyles = listOf("CALM"),
                mainImage = ImageResponse(imageId = 3L, url = "http://image-server.com/main2.jpg"),
                subImages = listOf(ImageResponse(imageId = 4L, url = "http://image-server.com/sub2.jpg")),
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

        every {
            productSearchUseCase.searchProducts(
                title = "Snap",
                productType = "WEDDING_SNAP",
                shootingPlace = "JEJU",
                sortBy = "created-desc",
                page = 0,
                size = 10,
            )
        } returns pagedResult

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
                    "data.content[].userId" type STRING means "사용자 ID",
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
                    "data.content[].createdAt" type OBJECT means "생성 시간",
                    "data.content[].updatedAt" type OBJECT means "수정 시간",
                    "data.content[].options" type ARRAY means "옵션 목록",
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
                userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                productType = "WEDDING_SNAP",
                shootingPlace = "BUSAN",
                title = "Busan Snap",
                description = "Desc",
                availableSeasons = listOf("YEAR_2025_FIRST_HALF"),
                cameraTypes = listOf("DIGITAL"),
                retouchStyles = listOf("NATURAL"),
                mainImage = ImageResponse(imageId = 11L, url = "http://image-server.com/main.jpg"),
                subImages = listOf(ImageResponse(imageId = 12L, url = "http://image-server.com/sub1.jpg")),
                additionalImages = emptyList(),
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

        every { productSearchUseCase.getMainPageProducts(page = 0, size = 10) } returns pagedResult

        val requestBuilder =
            RestDocumentationRequestBuilders
                .get("/api/products/main")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON)

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
                    "data.content[].userId" type STRING means "사용자 ID",
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
                    "data.content[].createdAt" type OBJECT means "생성 시간",
                    "data.content[].updatedAt" type OBJECT means "수정 시간",
                    "data.content[].options" type ARRAY means "옵션 목록",
                    "data.page" type NUMBER means "현재 페이지 번호",
                    "data.size" type NUMBER means "페이지 크기",
                    "data.totalElements" type NUMBER means "전체 원소 개수",
                    "data.totalPages" type NUMBER means "전체 페이지 개수",
                ),
            )
    }
}
