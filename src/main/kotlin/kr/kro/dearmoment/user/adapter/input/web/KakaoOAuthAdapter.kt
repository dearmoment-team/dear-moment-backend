package kr.kro.dearmoment.user.adapter.input.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.kro.dearmoment.common.constants.GlobalUrls
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.user.application.service.KakaoOAuthService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import java.util.UUID

@Tag(name = "Kakao Oauth API", description = "카카오 로그인 관련 API")
@RestController
class KakaoOAuthAdapter(
    private val kakaoOAuthService: KakaoOAuthService,
    @Value("\${oauth.kakao.redirect.success}")
    private val successRedirectUrl: String,
    @Value("\${oauth.kakao.redirect.failure}")
    private val failureRedirectUrl: String,
    @Value("\${oauth.kakao.client-id}")
    private var kakaoClientId: String,
    @Value("\${oauth.kakao.redirect-uri}")
    private var kakaoRedirectUri: String
) {
    @Operation(
        summary = "Kakao OAuth Callback",
        description = """
            카카오 로그인 동의 후 콜백 URL 입니다.
            카카오 서버로부터 전달받은 인가 코드를 이용하여 JWT 토큰을 발급한 후,
            프론트엔드의 성공 페이지로 리다이렉트하며 쿼리 파라미터에 accessToken 을 포함합니다.
            만약 인증 과정 중 예외 발생 시 실패 페이지로 리다이렉트합니다.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "302",
                description = "로그인 성공: JWT 토큰이 쿼리 파라미터 accessToken으로 첨부된 프론트엔드 성공 페이지로 리다이렉트합니다.",
                content = [
                    Content(
                        schema =
                            Schema(
                                example = """RedirectView("https://your-frontend.com/login/success?accessToken=<JWT_TOKEN>")"""
                            )
                    )
                ]
            )
        ]
    )
    @GetMapping(GlobalUrls.OAUTH_KAKAO_CALLBACK)
    fun kakaoCallback(
        @RequestParam("code") code: String
    ): RedirectView {
        return try {
            val jwtToken = kakaoOAuthService.kakaoLogin(code)
            // 로그인 성공 시, 액세스 토큰을 쿼리 파라미터로 포함한 성공 URL 로 리다이렉트
            RedirectView("$successRedirectUrl?accessToken=$jwtToken")
        } catch (e: CustomException) {
            // CustomException 이 발생한 경우, 에러 코드로 분리하여 실패 URL 로 리다이렉트
            RedirectView("$failureRedirectUrl?kakao=fail&error=${e.errorCode.name}")
        } catch (e: Exception) {
            // 기타 예외 발생 시 실패 URL 로 리다이렉트
            RedirectView("$failureRedirectUrl?error=unknown")
        }
    }

    @Operation(
        summary = "Kakao OAuth Redirect",
        description = "클라이언트의 요청을 Kakao OAuth 인증 페이지로 리다이렉트합니다. 이 엔드포인트를 통해 사용자가 카카오 로그인 절차를 진행할 수 있습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "302",
                description = "사용자를 Kakao 의 로그인 페이지로 리다이렉트합니다.",
                content = [Content(
                    schema =
                        Schema(
                            example = """RedirectView("https://kauth.kakao.com/oauth/authorize?client_id=1234512345123&redirect_uri=http://localhost:8080/oauth/kakao/callback&response_type=code")"""
                        )
                )]
            )
        ]
    )
    @GetMapping(GlobalUrls.OAUTH_KAKAO_REDIRECT)
    fun redirectToKakaoOauth(): RedirectView {
        val baseUrl = "https://kauth.kakao.com/oauth/authorize"
        val clientId = kakaoClientId
        val redirectUri = kakaoRedirectUri
        val responseType = "code"
        val redirectUrl = "$baseUrl?client_id=$clientId&redirect_uri=$redirectUri&response_type=$responseType"
        return RedirectView(redirectUrl)
    }

    /**
     * (예시) DELETE OAUTH_KAKAO_WITHDRAW
     * 헤더: Authorization: Bearer <카카오_사용자_액세스_토큰>
     */
    @Operation(summary = "카카오 연결 끊기 + 회원 탈퇴", description = "카카오에서 응답받은 id 값과 DB 매칭 후 하드딜리트")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "탈퇴 성공",
            ),
        ],
    )
    @DeleteMapping(GlobalUrls.OAUTH_KAKAO_WITHDRAW)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unlinkAndWithdraw(
        @AuthenticationPrincipal(expression = "id") userUuid: UUID,
    ) {
        kakaoOAuthService.unlinkAndWithdraw(userUuid)
    }
}
