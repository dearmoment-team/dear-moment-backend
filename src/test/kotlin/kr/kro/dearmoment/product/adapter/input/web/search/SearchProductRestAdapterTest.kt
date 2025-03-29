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
import kr.kro.dearmoment.common.restdocs.means
import kr.kro.dearmoment.common.restdocs.queryParameters
import kr.kro.dearmoment.common.restdocs.requestBody
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.toJsonString
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.product.application.dto.request.SearchProductRequest
import kr.kro.dearmoment.product.application.dto.response.SearchProductResponse
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SearchProductRestAdapterTest : RestApiTestBase() {
    @Test
    fun `상품 메인 페이지 조회 API`() {
        val requestBody = SearchProductRequest()

        val page = 0
        val size = 10
        val searchResults =
            listOf(
                SearchProductResponse(
                    productId = 1L,
                    studioName = "스튜디오 A",
                    thumbnailUrls = listOf("thumbnailA.url.com"),
                    retouchStyles = listOf("MODERN", "CALM"),
                    shootingSeason = listOf("2025_FIRST_HALF"),
                    minPrice = 800_000L,
                    maxPrice = 1_000_000L,
                    discountRate = 10,
                    isLike = true,
                ),
                SearchProductResponse(
                    productId = 2L,
                    studioName = "스튜디오 B",
                    thumbnailUrls = listOf("thumbnailB.url.com"),
                    retouchStyles = listOf("CHIC", "WARM"),
                    shootingSeason = listOf("2026_FIRST_HALF"),
                    minPrice = 1_200_000L,
                    maxPrice = 1_500_000L,
                    discountRate = 15,
                    isLike = false,
                ),
            )

        val response =
            PagedResponse(
                content = searchResults,
                page = page,
                size = size,
                totalElements = searchResults.size.toLong(),
                totalPages = 1,
            )

        every { productSearchUseCase.searchProducts(requestBody, page, size) } returns response

        val request =
            RestDocumentationRequestBuilders.get("/api/products/main")
                .param("page", page.toString())
                .param("size", size.toString())
                .contentType(MediaType.APPLICATION_JSON)

        // When & Then
        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "search-products-main",
                queryParameters(
                    "page" means "조회할 페이지 번호 (0부터 시작)",
                    "size" means "페이지 크기 (기본값: 10)",
                    "_csrf" means "스프링 시큐리티 사용시 테스트에 넘겨주는 토큰",
                ),
                responseBody(
                    "data" type OBJECT means "응답 데이터",
                    "data.content" type ARRAY means "상품 리스트",
                    "data.content[].productId" type NUMBER means "상품 ID",
                    "data.content[].studioName" type STRING means "스튜디오 이름",
                    "data.content[].thumbnailUrls" type ARRAY means "스튜디오 썸네일 이미지 URL 목록",
                    "data.content[].minPrice" type NUMBER means "최소 가격",
                    "data.content[].maxPrice" type NUMBER means "최대 가격",
                    "data.content[].discountRate" type NUMBER means "할인율",
                    "data.content[].shootingSeason" type ARRAY means "촬영 가능 시기 목록",
                    "data.content[].retouchStyles" type ARRAY means "보정 스타일 목록",
                    "data.content[].isLike" type BOOLEAN means "좋아요 여부",
                    "data.totalPages" type NUMBER means "전체 페이지 수",
                    "data.totalElements" type NUMBER means "전체 데이터 개수",
                    "data.size" type NUMBER means "페이지 크기",
                    "data.page" type NUMBER means "현재 페이지 번호",
                    "success" type BOOLEAN means "성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                ),
            )
    }

    @Test
    fun `상품 필터 검색 API`() {
        val requestBody =
            SearchProductRequest(
                sortBy = "POPULAR",
                availableSeasons = listOf("2025_FIRST_HALF", "2026_FIRST_HALF"),
                retouchStyles = listOf("MODERN", "CALM", "CHIC", "WARM"),
                cameraTypes = listOf("DIGITAL"),
                partnerShopCategories = listOf("HAIR_MAKEUP", "DRESS", "MENS_SUIT"),
                minPrice = 800_000L,
                maxPrice = 1_500_000L,
            )

        val page = 0
        val size = 10
        val searchResults =
            listOf(
                SearchProductResponse(
                    productId = 1L,
                    studioName = "스튜디오 A",
                    thumbnailUrls = listOf("thumbnailA.url.com"),
                    retouchStyles = listOf("MODERN", "CALM"),
                    shootingSeason = listOf("2025_FIRST_HALF"),
                    minPrice = 800_000L,
                    maxPrice = 1_000_000L,
                    discountRate = 10,
                    isLike = true,
                ),
                SearchProductResponse(
                    productId = 2L,
                    studioName = "스튜디오 B",
                    thumbnailUrls = listOf("thumbnailB.url.com"),
                    retouchStyles = listOf("CHIC", "WARM"),
                    shootingSeason = listOf("2026_FIRST_HALF"),
                    minPrice = 1_200_000L,
                    maxPrice = 1_500_000L,
                    discountRate = 15,
                    isLike = false,
                ),
            )

        val response =
            PagedResponse(
                content = searchResults,
                page = page,
                size = size,
                totalElements = searchResults.size.toLong(),
                totalPages = 1,
            )

        every { productSearchUseCase.searchProducts(requestBody, page, size) } returns response

        val request =
            RestDocumentationRequestBuilders.get("/api/products/search")
                .param("page", page.toString())
                .param("size", size.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody.toJsonString())

        // When & Then
        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "search-products-filter",
                requestBody(
                    "sortBy" type STRING means "정렬 기준",
                    "availableSeasons" type ARRAY means "촬영 시기",
                    "retouchStyles" type ARRAY means "보정 스타일 목록",
                    "partnerShopCategories" type ARRAY means "패키지 목록",
                    "cameraTypes" type ARRAY means "촬영 카메라 타입",
                    "minPrice" type NUMBER means "최소 가격",
                    "maxPrice" type NUMBER means "최대 가격",
                ),
                queryParameters(
                    "page" means "조회할 페이지 번호 (0부터 시작)",
                    "size" means "페이지 크기 (기본값: 10)",
                    "_csrf" means "스프링 시큐리티 사용시 테스트에 넘겨주는 토큰",
                ),
                responseBody(
                    "data" type OBJECT means "응답 데이터",
                    "data.content" type ARRAY means "상품 리스트",
                    "data.content[].productId" type NUMBER means "상품 ID",
                    "data.content[].studioName" type STRING means "스튜디오 이름",
                    "data.content[].thumbnailUrls" type ARRAY means "스튜디오 썸네일 이미지 URL 목록",
                    "data.content[].minPrice" type NUMBER means "최소 가격",
                    "data.content[].maxPrice" type NUMBER means "최대 가격",
                    "data.content[].discountRate" type NUMBER means "할인율",
                    "data.content[].shootingSeason" type ARRAY means "촬영 가능 시기 목록",
                    "data.content[].retouchStyles" type ARRAY means "보정 스타일 목록",
                    "data.content[].isLike" type BOOLEAN means "좋아요 여부",
                    "data.totalPages" type NUMBER means "전체 페이지 수",
                    "data.totalElements" type NUMBER means "전체 데이터 개수",
                    "data.size" type NUMBER means "페이지 크기",
                    "data.page" type NUMBER means "현재 페이지 번호",
                    "success" type BOOLEAN means "성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                ),
            )
    }
}
