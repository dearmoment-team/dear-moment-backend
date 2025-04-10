package kr.kro.dearmoment.like.adapter.input.web

import andDocument
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
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
import kr.kro.dearmoment.like.application.command.SaveLikeCommand
import kr.kro.dearmoment.like.application.dto.FilterUserLikesRequest
import kr.kro.dearmoment.like.application.dto.GetProductOptionLikeResponse
import kr.kro.dearmoment.like.application.dto.LikeRequest
import kr.kro.dearmoment.like.application.dto.LikeResponse
import kr.kro.dearmoment.like.application.dto.UnlikeProductOptionRequest
import kr.kro.dearmoment.like.application.query.GetUserProductOptionLikeQuery
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ProductOptionLikeRestAdapterTest : RestApiTestBase() {
    @Test
    fun `상품 옵션 좋아요 생성 API`() {
        val requestBody =
            LikeRequest(
                targetId = 1L,
            )

        val command = SaveLikeCommand(userId, requestBody.targetId)
        val expectedResponse = LikeResponse(likeId = 1L)

        every { likeUseCase.productOptionsLike(command) } returns expectedResponse

        val request =
            RestDocumentationRequestBuilders
                .post("/api/likes/product-options")
                .content(requestBody.toJsonString())
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "create-product-options-like",
                requestBody(
                    "userId" type NUMBER means "유저 ID",
                    "targetId" type NUMBER means "좋아요할 상품 ID",
                ),
                responseBody(
                    "data" type OBJECT means "데이터",
                    "data.likeId" type NUMBER means "좋아요 ID",
                    "success" type BOOLEAN means "성공여부",
                    "code" type NUMBER means "HTTP 코드",
                ),
            )
    }

    @Test
    fun `유저 상품 옵션 좋아요 조회 API`() {
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))

        val userLikes =
            listOf(
                GetProductOptionLikeResponse(
                    likeId = 1L,
                    optionName = "상품 옵션 A",
                    productId = 1L,
                    productOptionId = 1L,
                    studioName = "스튜디오 B",
                    price = 1_000_000L,
                    discountRate = 20,
                    thumbnailUrl = "thumbnailA.url.com",
                    shootingHours = 3,
                    originalProvided = true,
                    shootingLocationCount = 3,
                    shootingSeason = listOf("YEAR_2025_FIRST_HALF", "YEAR_2026_FIRST_HALF"),
                    costumeCount = 10,
                    retouchedCount = 2,
                ),
                GetProductOptionLikeResponse(
                    likeId = 2L,
                    optionName = "상품 옵션 B",
                    productId = 1L,
                    productOptionId = 2L,
                    studioName = "스튜디오 B",
                    price = 800_000L,
                    discountRate = 24,
                    thumbnailUrl = "thumbnailB.url.com",
                    shootingHours = 4,
                    originalProvided = false,
                    shootingLocationCount = 4,
                    shootingSeason = listOf("YEAR_2025_SECOND_HALF", "YEAR_2026_SECOND_HALF"),
                    costumeCount = 9,
                    retouchedCount = 3,
                ),
            )

        val page = PageImpl(userLikes, pageable, userLikes.size.toLong())
        val expectedResponse =
            PagedResponse(
                content = page.content,
                page = page.number,
                size = page.size,
            )

        every {
            likeQueryService.getUserProductOptionLikes(
                GetUserProductOptionLikeQuery(
                    userId,
                    pageable,
                ),
            )
        } returns expectedResponse

        val request =
            RestDocumentationRequestBuilders
                .get("/api/likes/product-options")

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "get-user-product-option-likes",
                responseBody(
                    "data" type OBJECT means "응답 데이터 배열",
                    "data.content" type ARRAY means "유저 상품 옵션 좋아요 리스트",
                    "data.content[].likeId" type NUMBER means "좋아요 ID",
                    "data.content[].optionName" type STRING means "상품 옵션 이름",
                    "data.content[].productId" type NUMBER means "상품 ID",
                    "data.content[].productOptionId" type NUMBER means "상품 옵션 ID",
                    "data.content[].studioName" type STRING means "스튜디오 이름",
                    "data.content[].price" type NUMBER means "상품 옵션 가격",
                    "data.content[].discountRate" type NUMBER means "상품 옵션 할인율",
                    "data.content[].thumbnailUrl" type STRING means "상품 옵션 썸네일 URL",
                    "data.content[].shootingHours" type NUMBER means "촬영 시간 (시간 단위)",
                    "data.content[].originalProvided" type BOOLEAN means "원본 제공 여부",
                    "data.content[].shootingLocationCount" type NUMBER means "촬영 장소 개수",
                    "data.content[].costumeCount" type NUMBER means "의상 개수",
                    "data.content[].retouchedCount" type NUMBER means "보정본 제공 개수",
                    "data.content[].shootingSeason" type ARRAY means "촬영 가능 시기 목록",
                    "data.size" type NUMBER means "페이지 크기",
                    "data.page" type NUMBER means "현재 페이지 번호",
                    "success" type BOOLEAN means "성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                ),
            )
    }

    @Test
    fun `유저 상품 옵션 좋아요 필터 API`() {
        val expectedResponse =
            listOf(
                GetProductOptionLikeResponse(
                    likeId = 1L,
                    optionName = "상품 옵션 A",
                    productId = 1L,
                    productOptionId = 1L,
                    studioName = "스튜디오 B",
                    price = 1_000_000L,
                    discountRate = 20,
                    thumbnailUrl = "thumbnailA.url.com",
                    shootingHours = 3,
                    originalProvided = true,
                    shootingLocationCount = 3,
                    shootingSeason = listOf("YEAR_2025_FIRST_HALF", "YEAR_2026_FIRST_HALF"),
                    costumeCount = 10,
                    retouchedCount = 2,
                ),
                GetProductOptionLikeResponse(
                    likeId = 2L,
                    optionName = "상품 옵션 B",
                    productId = 1L,
                    productOptionId = 2L,
                    studioName = "스튜디오 B",
                    price = 800_000L,
                    discountRate = 24,
                    thumbnailUrl = "thumbnailB.url.com",
                    shootingHours = 4,
                    originalProvided = false,
                    shootingLocationCount = 4,
                    shootingSeason = listOf("YEAR_2025_SECOND_HALF", "YEAR_2026_SECOND_HALF"),
                    costumeCount = 9,
                    retouchedCount = 3,
                ),
            )

        val filterRequest =
            FilterUserLikesRequest(
                sortBy = "POPULAR",
                availableSeasons = listOf("YEAR_2025_FIRST_HALF", "YEAR_2026_FIRST_HALF"),
                retouchStyles = listOf("MODERN", "CALM", "CHIC", "WARM"),
                cameraTypes = listOf("DIGITAL"),
                partnerShopCategories = listOf("HAIR_MAKEUP", "DRESS", "MENS_SUIT"),
                minPrice = 800_000L,
                maxPrice = 1_500_000L,
            )

        every { likeQueryService.filterUserProductsOptionsLikes(userId, filterRequest.toQuery()) } returns expectedResponse

        val request =
            RestDocumentationRequestBuilders
                .get("/api/likes/product-options/filter")
                .param("minPrice", filterRequest.minPrice.toString())
                .param("maxPrice", filterRequest.maxPrice.toString())
                .param("availableSeasons", *filterRequest.availableSeasons.toTypedArray())
                .param("retouchStyles", *filterRequest.retouchStyles.toTypedArray())
                .param("partnerShopCategories", *filterRequest.partnerShopCategories.toTypedArray())
                .param("cameraTypes", *filterRequest.cameraTypes.toTypedArray())
                .param("sortBy", filterRequest.sortBy)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "filter-user-product-option-likes",
                queryParameters(
                    "sortBy" means "정렬 기준",
                    "availableSeasons" means "촬영 시기",
                    "retouchStyles" means "보정 스타일 목록",
                    "partnerShopCategories" means "패키지 목록",
                    "cameraTypes" means "촬영 카메라 타입",
                    "minPrice" means "최소 가격",
                    "maxPrice" means "최대 가격",
                    "_csrf" means "스프링 시큐리티 사용시 테스트에 넘겨주는 토큰",
                ),
                responseBody(
                    "data[]" type ARRAY means "유저 상품 옵션 좋아요 리스트",
                    "data[].likeId" type NUMBER means "좋아요 ID",
                    "data[].optionName" type STRING means "상품 옵션 이름",
                    "data[].productId" type NUMBER means "상품 ID",
                    "data[].productOptionId" type NUMBER means "상품 옵션 ID",
                    "data[].studioName" type STRING means "스튜디오 이름",
                    "data[].price" type NUMBER means "상품 옵션 가격",
                    "data[].discountRate" type NUMBER means "상품 옵션 할인율",
                    "data[].thumbnailUrl" type STRING means "상품 옵션 썸네일 URL",
                    "data[].shootingHours" type NUMBER means "촬영 시간 (시간 단위)",
                    "data[].originalProvided" type BOOLEAN means "원본 제공 여부",
                    "data[].shootingLocationCount" type NUMBER means "촬영 장소 개수",
                    "data[].costumeCount" type NUMBER means "의상 개수",
                    "data[].retouchedCount" type NUMBER means "보정본 제공 개수",
                    "data[].shootingSeason" type ARRAY means "촬영 가능 시기 목록",
                    "success" type BOOLEAN means "성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                ),
            )
    }

    @Test
    fun `상품 옵션 좋아요 삭제 API`() {
        val requestBody =
            UnlikeProductOptionRequest(
                likeId = 1L,
                productOptionId = 1L,
            )
        every { likeUseCase.productOptionUnlike(requestBody.toCommand(userId)) } just Runs

        val request =
            RestDocumentationRequestBuilders
                .delete("/api/likes/product-options")
                .content(requestBody.toJsonString())
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andExpect(status().isNoContent)
            .andDocument(
                "delete-products-like",
                requestBody(
                    "likeId" type NUMBER means "삭제할 문의 ID",
                    "productOptionId" type NUMBER means "상품 옵션 ID",
                ),
            )
    }
}
