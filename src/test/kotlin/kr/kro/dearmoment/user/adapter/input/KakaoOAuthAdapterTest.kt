package kr.kro.dearmoment.user.adapter.input

import andDocument
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.kro.dearmoment.common.RestApiTestBase
import kr.kro.dearmoment.common.constants.GlobalUrls
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID
import kotlin.test.Test

class KakaoOAuthAdapterTest : RestApiTestBase() {
    @Test
    fun `카카오 유저 회원 탈퇴 API`() {
        // 가정: 현재 로그인 중인 사용자의 UUID
        val userUuid = UUID.randomUUID()

        // kakaoOAuthService.unlinkAndWithdraw(...)를 목킹
        every { kakaoOAuthService.unlinkAndWithdraw(userUuid) } just runs

        // DELETE 요청 생성
        val request =
            RestDocumentationRequestBuilders
                .delete(GlobalUrls.OAUTH_KAKAO_WITHDRAW)
                // 문서화에서 "Authorization" 헤더를 기록하고 싶다면,
                // 실제 요청에도 이 헤더를 추가해야 REST Docs 검증을 통과함
                .header("Authorization", "Bearer test_jwt_token")

        // withAuthenticatedUser(...)로 시큐리티 컨텍스트에 userUuid 주입
        mockMvc.perform(withAuthenticatedUser(userUuid, request))
            .andExpect(status().isNoContent)
            .andDocument(
                "delete-kakao-user",
                requestHeaders(
                    headerWithName("Authorization")
                        .description("JWT 토큰 (Bearer {TOKEN})"),
                ),
            )
    }
}
