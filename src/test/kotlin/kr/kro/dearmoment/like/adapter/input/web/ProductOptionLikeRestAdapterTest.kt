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
import kr.kro.dearmoment.common.restdocs.requestBody
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.toJsonString
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.like.application.command.SaveLikeCommand
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
                    productOptionId = 1L,
                    studioName = "스튜디오 B",
                    price = 1_000_000L,
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
                    productOptionId = 2L,
                    studioName = "스튜디오 B",
                    price = 800_000L,
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
                    "data.content[].productOptionId" type NUMBER means "상품 ID",
                    "data.content[].studioName" type STRING means "스튜디오 이름",
                    "data.content[].price" type NUMBER means "상품 옵션 가격",
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
