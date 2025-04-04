package kr.kro.dearmoment.user.adapter.input.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import kr.kro.dearmoment.common.constants.GlobalUrls
import kr.kro.dearmoment.user.application.dto.response.LoginUserResponse
import kr.kro.dearmoment.user.application.service.KakaoOAuthService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Kakao Oauth API", description = "카카오 로그인 관련 API")
@RestController
class KakaoOAuthAdapter(
    private val kakaoOAuthService: KakaoOAuthService,
) {
    @Operation(
        summary = "카카오 OAuth 콜백",
        description = "카카오 로그인 동의 후 리다이렉트되는 콜백 URL. code 파라미터로 JWT 를 발급받아 반환합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "JWT 발급 성공",
                content = [
                    Content(
                        schema =
                            Schema(
                                example = """{"success":true,"code":200,"data":{"success":true}}""",
                            ),
                    ),
                ],
            ),
        ],
    )
    @GetMapping(GlobalUrls.OAUTH_KAKAO_CALLBACK)
    fun kakaoCallback(
        @Parameter(description = "카카오에서 전달받은 인가 코드", required = true)
        @RequestParam("code") code: String,
        response: HttpServletResponse,
    ): LoginUserResponse {
        val jwtToken = kakaoOAuthService.kakaoLogin(code)
        response.setHeader("Authorization", "Bearer $jwtToken")
        return LoginUserResponse(success = true)
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
