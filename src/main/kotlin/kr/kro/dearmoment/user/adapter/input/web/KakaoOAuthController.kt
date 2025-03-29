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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Kakao Oauth API", description = "카카오 로그인 관련 API")
@RestController
class KakaoOAuthController(
    private val kakaoOAuthService: KakaoOAuthService,
) {
    @Operation(
        summary = "카카오 OAuth 콜백",
        description = "카카오 로그인 동의 후 리다이렉트되는 콜백 URL. code 파라미터로 JWT를 발급받아 반환합니다.",
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
}
