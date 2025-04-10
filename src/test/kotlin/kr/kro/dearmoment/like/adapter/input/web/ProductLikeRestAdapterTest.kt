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
import kr.kro.dearmoment.like.application.dto.GetProductLikeResponse
import kr.kro.dearmoment.like.application.dto.LikeRequest
import kr.kro.dearmoment.like.application.dto.LikeResponse
import kr.kro.dearmoment.like.application.dto.UnlikeProductRequest
import kr.kro.dearmoment.like.application.query.GetUserProductLikeQuery
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ProductLikeRestAdapterTest : RestApiTestBase() {
    @Test
    fun `상품 좋아요 생성 API`() {
        val requestBody =
            LikeRequest(
                targetId = 1L,
            )
        val command = requestBody.toCommand(userId)
        val expectedResponse = LikeResponse(likeId = 1L)

        every { likeUseCase.productLike(command) } returns expectedResponse

        val request =
            RestDocumentationRequestBuilders
                .post("/api/likes/products")
                .content(requestBody.toJsonString())
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "create-products-like",
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
    fun `유저 상품 좋아요 조회 API`() {
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
        val userLikes =
            listOf(
                GetProductLikeResponse(
                    likeId = 1L,
                    productId = 1L,
                    name = "스튜디오 A",
                    thumbnailUrls =
                        listOf(
                            "thumbnailA-A.url.com",
                            "thumbnailA-B.url.com",
                            "thumbnailA-C.url.com",
                        ),
                    minPrice = 800_000L,
                    maxPrice = 1_000_000L,
                    discountRate = 10,
                    availableSeasons = listOf(ShootingSeason.YEAR_2025_FIRST_HALF.name),
                    retouchStyles = listOf(RetouchStyle.MODERN.name, RetouchStyle.CALM.name),
                ),
                GetProductLikeResponse(
                    likeId = 2L,
                    productId = 2L,
                    name = "스튜디오 B",
                    thumbnailUrls =
                        listOf(
                            "thumbnailB-A.url.com",
                            "thumbnailB-B.url.com",
                            "thumbnailB-C.url.com",
                        ),
                    minPrice = 1_000_000L,
                    maxPrice = 2_000_000L,
                    discountRate = 50,
                    availableSeasons = listOf(ShootingSeason.YEAR_2026_FIRST_HALF.name),
                    retouchStyles = listOf(RetouchStyle.CHIC.name, RetouchStyle.WARM.name),
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
            likeQueryService.getUserProductLikes(
                GetUserProductLikeQuery(
                    userId,
                    pageable,
                ),
            )
        } returns expectedResponse

        val request =
            RestDocumentationRequestBuilders
                .get("/api/likes/products")

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "get-user-studio-likes",
                responseBody(
                    "data" type OBJECT means "응답 데이터 배열",
                    "data.content" type ARRAY means "유저 상품 좋아요 리스트",
                    "data.content[].likeId" type NUMBER means "좋아요 ID",
                    "data.content[].productId" type NUMBER means "상품 ID",
                    "data.content[].name" type STRING means "스튜디오 이름",
                    "data.content[].thumbnailUrls" type ARRAY means "스튜디오 썸네일 이미지 URL 목록",
                    "data.content[].minPrice" type NUMBER means "최소 가격",
                    "data.content[].maxPrice" type NUMBER means "최대 가격",
                    "data.content[].discountRate" type NUMBER means "할인율",
                    "data.content[].availableSeasons" type ARRAY means "촬영 가능 시기 목록",
                    "data.content[].retouchStyles" type ARRAY means "보정 스타일 목록",
                    "data.size" type NUMBER means "페이지 크기",
                    "data.page" type NUMBER means "현재 페이지 번호",
                    "success" type BOOLEAN means "성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                ),
            )
    }

    @Test
    fun `상품 좋아요 삭제 API`() {
        val requestBody =
            UnlikeProductRequest(
                likeId = 1L,
                productId = 1L,
            )

        every { likeUseCase.productUnlike(requestBody.toCommand(userId)) } just Runs

        val request =
            RestDocumentationRequestBuilders
                .delete("/api/likes/products")
                .content(requestBody.toJsonString())
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andExpect(status().isNoContent)
            .andDocument(
                "delete-products-like",
                requestBody(
                    "likeId" type NUMBER means "삭제할 문의 ID",
                    "productId" type NUMBER means "상품 ID",
                ),
            )
    }
}
