package kr.kro.dearmoment.like.adapter.input.web

import andDocument
import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kr.kro.dearmoment.common.RestApiTestBase
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
import kr.kro.dearmoment.like.adapter.input.web.dto.LikeRequest
import kr.kro.dearmoment.like.adapter.input.web.dto.LikeResponse
import kr.kro.dearmoment.like.application.command.LikeCommand
import kr.kro.dearmoment.like.application.port.input.LikeUseCase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class LikeRestAdapterTest : RestApiTestBase() {
    @MockkBean
    lateinit var likeUseCase: LikeUseCase

    @Test
    fun `좋아요 생성 API`() {
        val requestBody =
            LikeRequest(
                userId = 1L,
                targetId = 1L,
                type = "AUTHOR",
            )

        val command = LikeCommand(requestBody.userId, requestBody.targetId, requestBody.type)
        val expectedResponse = LikeResponse(likeId = 1L)

        every { likeUseCase.like(command) } returns expectedResponse

        val request =
            RestDocumentationRequestBuilders
                .post("/api/likes")
                .content(requestBody.toJsonString())
                .contentType(MediaType.APPLICATION_JSON)

        mockMvc.perform(request)
            .andExpect(status().isOk)
            .andDocument(
                "create-like",
                requestBody(
                    "userId" type NUMBER means "유저 ID",
                    "targetId" type NUMBER means "좋아요할 대상 ID(상품, 작가)",
                    "type" type STRING means "좋아요 타입(\"AUTHOR\", \"PRODUCT\"",
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
    fun `좋아요 삭제 API`() {
        val likeId = 1L
        every { likeUseCase.unlike(likeId) } just Runs

        val request =
            RestDocumentationRequestBuilders
                .delete("/api/likes/{id}", likeId)

        mockMvc.perform(request)
            .andExpect(status().isNoContent)
            .andDocument(
                "delete-like",
                pathParameters(
                    "id" means "삭제할 좋아요 ID",
                ),
            )
    }
}
