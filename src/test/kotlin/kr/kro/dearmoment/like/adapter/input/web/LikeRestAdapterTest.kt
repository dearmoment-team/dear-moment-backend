package kr.kro.dearmoment.like.adapter.input.web

import andDocument
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.restdocs.ARRAY
import kr.kro.dearmoment.common.restdocs.BOOLEAN
import kr.kro.dearmoment.common.restdocs.NUMBER
import kr.kro.dearmoment.common.restdocs.OBJECT
import kr.kro.dearmoment.common.restdocs.STRING
import kr.kro.dearmoment.common.restdocs.means
import kr.kro.dearmoment.common.restdocs.pathParameters
import kr.kro.dearmoment.common.restdocs.requestBody
import kr.kro.dearmoment.common.restdocs.responseBody
import kr.kro.dearmoment.common.restdocs.toJsonString
import kr.kro.dearmoment.common.restdocs.type
import kr.kro.dearmoment.like.application.command.SaveLikeCommand
import kr.kro.dearmoment.like.application.dto.GetProductOptionLikeResponse
import kr.kro.dearmoment.like.application.dto.GetStudioLikeResponse
import kr.kro.dearmoment.like.application.dto.LikeRequest
import kr.kro.dearmoment.like.application.dto.LikeResponse
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class LikeRestAdapterTest : RestApiTestBase() {
    @Test
    fun `스튜디오 좋아요 생성 API`() {
        val requestBody =
            LikeRequest(
                userId = 1L,
                targetId = 1L,
            )

        val command = SaveLikeCommand(requestBody.userId, requestBody.targetId)
        val expectedResponse = LikeResponse(likeId = 1L)

        every { likeUseCase.studioLike(command) } returns expectedResponse

        val request =
            RestDocumentationRequestBuilders
                .post("/api/likes/studios")
                .content(requestBody.toJsonString())
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "create-studios-like",
                requestBody(
                    "userId" type NUMBER means "유저 ID",
                    "targetId" type NUMBER means "좋아요할 스튜디오 ID",
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
    fun `상품 좋아요 생성 API`() {
        val requestBody =
            LikeRequest(
                userId = 1L,
                targetId = 1L,
            )

        val command = SaveLikeCommand(requestBody.userId, requestBody.targetId)
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
        val expectedResponse =
            listOf(
                GetProductOptionLikeResponse(
                    likeId = 1L,
                    optionName = "상품 옵션 A",
                    productId = 1L,
                    studioName = "스튜디오 B",
                    price = 1_000_000L,
                    thumbnailUrl = "thumbnailA.url.com",
                    shootingHours = 3,
                    originalProvided = true,
                    shootingLocationCount = 3,
                    costumeCount = 10,
                    retouchedCount = 2,
                ),
                GetProductOptionLikeResponse(
                    likeId = 2L,
                    optionName = "상품 옵션 B",
                    productId = 2L,
                    studioName = "스튜디오 B",
                    price = 800_000L,
                    thumbnailUrl = "thumbnailB.url.com",
                    shootingHours = 4,
                    originalProvided = false,
                    shootingLocationCount = 4,
                    costumeCount = 9,
                    retouchedCount = 3,
                ),
            )

        val userId = 1L
        every { likeQueryService.getUserProductOptionLikes(userId) } returns expectedResponse

        val request =
            RestDocumentationRequestBuilders
                .get("/api/likes/product-options/{userId}", userId)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "get-user-product-option-likes",
                pathParameters(
                    "userId" means "조회할 유저 ID",
                ),
                responseBody(
                    "data" type ARRAY means "응답 데이터 배열",
                    "data[].likeId" type NUMBER means "좋아요 ID",
                    "data[].optionName" type STRING means "상품 옵션 이름",
                    "data[].productId" type NUMBER means "상품 ID",
                    "data[].studioName" type STRING means "스튜디오 이름",
                    "data[].price" type NUMBER means "상품 옵션 가격",
                    "data[].thumbnailUrl" type STRING means "상품 옵션 썸네일 URL",
                    "data[].shootingHours" type NUMBER means "촬영 시간 (시간 단위)",
                    "data[].originalProvided" type BOOLEAN means "원본 제공 여부",
                    "data[].shootingLocationCount" type NUMBER means "촬영 장소 개수",
                    "data[].costumeCount" type NUMBER means "의상 개수",
                    "data[].retouchedCount" type NUMBER means "보정본 제공 개수",
                    "success" type BOOLEAN means "성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                ),
            )
    }

    @Test
    fun `유저 스튜디오 좋아요 조회 API`() {
        val expectedResponse =
            listOf(
                GetStudioLikeResponse(
                    likeId = 1L,
                    studioId = 1L,
                    name = "스튜디오 A",
                    thumbnailUrls =
                        listOf(
                            "thumbnailA-A.url.com",
                            "thumbnailA-B.url.com",
                            "thumbnailA-C.url.com",
                        ),
                    minPrice = 800_000L,
                    maxPrice = 1_000_000L,
                    availableSeasons = listOf(ShootingSeason.YEAR_2025_FIRST_HALF.name),
                    retouchStyles = listOf(RetouchStyle.MODERN.name, RetouchStyle.CALM.name),
                ),
                GetStudioLikeResponse(
                    likeId = 2L,
                    studioId = 2L,
                    name = "스튜디오 B",
                    thumbnailUrls =
                        listOf(
                            "thumbnailB-A.url.com",
                            "thumbnailB-B.url.com",
                            "thumbnailB-C.url.com",
                        ),
                    minPrice = 1_000_000L,
                    maxPrice = 2_000_000L,
                    availableSeasons = listOf(ShootingSeason.YEAR_2026_FIRST_HALF.name),
                    retouchStyles = listOf(RetouchStyle.CHIC.name, RetouchStyle.WARM.name),
                ),
            )
        val userId = 1L
        every { likeQueryService.getUserStudioLikes(userId) } returns expectedResponse

        val request =
            RestDocumentationRequestBuilders
                .get("/api/likes/studios/{userId}", userId)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "get-user-studio-likes",
                pathParameters(
                    "userId" means "조회할 유저 ID",
                ),
                responseBody(
                    "data" type ARRAY means "응답 데이터 배열",
                    "data[].likeId" type NUMBER means "좋아요 ID",
                    "data[].studioId" type NUMBER means "스튜디오 ID",
                    "data[].name" type STRING means "스튜디오 이름",
                    "data[].thumbnailUrls" type ARRAY means "스튜디오 썸네일 이미지 URL 목록",
                    "data[].minPrice" type NUMBER means "최소 가격",
                    "data[].maxPrice" type NUMBER means "최대 가격",
                    "data[].availableSeasons" type ARRAY means "촬영 가능 시기 목록",
                    "data[].retouchStyles" type ARRAY means "보정 스타일 목록",
                    "success" type BOOLEAN means "성공 여부",
                    "code" type NUMBER means "HTTP 상태 코드",
                ),
            )
    }

    @Test
    fun `스튜디오 좋아요 삭제 API`() {
        val likeId = 1L
        every { likeUseCase.studioUnlike(likeId) } just Runs

        val request =
            RestDocumentationRequestBuilders
                .delete("/api/likes/studios/{id}", likeId)

        mockMvc.perform(request)
            .andExpect(status().isNoContent)
            .andDocument(
                "delete-studios-like",
                pathParameters(
                    "id" means "삭제할 스튜디오 좋아요 ID",
                ),
            )
    }

    @Test
    fun `상품 좋아요 삭제 API`() {
        val likeId = 1L
        every { likeUseCase.productOptionUnlike(likeId) } just Runs

        val request =
            RestDocumentationRequestBuilders
                .delete("/api/likes/product-options/{id}", likeId)

        mockMvc.perform(request)
            .andExpect(status().isNoContent)
            .andDocument(
                "delete-product-options-like",
                pathParameters(
                    "id" means "삭제할 상품 좋아요 ID",
                ),
            )
    }
}
