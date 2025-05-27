package kr.kro.dearmoment.user.adapter.input

import andDocument
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.constants.GlobalUrls
import kr.kro.dearmoment.user.application.dto.request.RegisterWithdrawalFeedbackRequest
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID
import kotlin.test.Test

class KakaoOAuthAdapterTest : RestApiTestBase() {
    private val om = jacksonObjectMapper()

    @Test
    fun `카카오 유저 회원 탈퇴 API`() {
        // ---------- given ----------
        val userUuid = UUID.randomUUID()
        val reqBody =
            RegisterWithdrawalFeedbackRequest(
                reasonCode = 6,
                customReason = "테스트 기타 사유"
            )
        val json = om.writeValueAsString(reqBody)

        every { kakaoOAuthService.unlinkAndWithdraw(userUuid, any()) } just runs

        // ---------- when ----------
        val request =
            RestDocumentationRequestBuilders
                .delete(GlobalUrls.OAUTH_KAKAO_WITHDRAW)
                .header("Authorization", "Bearer test_jwt_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)

        // ---------- then ----------
        mockMvc.perform(withAuthenticatedUser(userUuid, request))
            .andExpect(status().isNoContent)
            .andDocument(
                "delete-kakao-user",
                requestHeaders(
                    headerWithName("Authorization").description("JWT 토큰 (Bearer {TOKEN})")
                ),
                requestFields(
                    fieldWithPath("reasonCode").description("탈퇴 사유 코드 (1‒6)"),
                    fieldWithPath("customReason").description("기타 사유 (코드 6일 때)").optional()
                )
            )
    }
}
