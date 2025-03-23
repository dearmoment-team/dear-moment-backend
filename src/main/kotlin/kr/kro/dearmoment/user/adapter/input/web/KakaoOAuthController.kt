package kr.kro.dearmoment.user.adapter.input.web

import io.swagger.v3.oas.annotations.tags.Tag
import kr.kro.dearmoment.user.application.service.KakaoOAuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Kakao Oauth API", description = "카카오 로그인 관련 API")
@RestController
class KakaoOAuthController(
    private val kakaoOAuthService: KakaoOAuthService,
) {
    /**
     * 카카오에서 인가코드를 받고 리다이렉트되는 URL
     * 예: http://localhost:8080/oauth/kakao/callback?code=xxxx
     */
    @GetMapping("/oauth/kakao/callback")
    fun kakaoCallback(
        @RequestParam("code") code: String,
    ): ResponseEntity<Map<String, Any>> {
        val jwtToken = kakaoOAuthService.kakaoLogin(code)
        // 실제로는 JWT를 쿠키에 담아주거나, Redirect로 프론트엔드에 보내는 등
        // 다양한 방식을 적용할 수 있음
        return ResponseEntity.ok(mapOf("token" to jwtToken, "message" to "Kakao Login Success"))
    }
}
