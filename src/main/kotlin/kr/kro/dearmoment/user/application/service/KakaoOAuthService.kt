// KakaoOAuthService.kt
package kr.kro.dearmoment.user.application.service

import kr.kro.dearmoment.security.JwtTokenProvider
import kr.kro.dearmoment.user.adapter.output.oauth.KakaoOAuthApiClient
import kr.kro.dearmoment.user.application.port.output.GetUserByKakaoIdPort
import kr.kro.dearmoment.user.application.port.output.SaveUserPort
import kr.kro.dearmoment.user.domain.User
import kr.kro.dearmoment.user.security.CustomUserDetails
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class KakaoOAuthService(
    private val kakaoOAuthApiClient: KakaoOAuthApiClient,
    private val getUserByKakaoIdPort: GetUserByKakaoIdPort,
    private val saveUserPort: SaveUserPort,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    @Value("\${oauth.kakao.client-id}")
    lateinit var kakaoClientId: String

    @Value("\${oauth.kakao.redirect-uri}")
    lateinit var kakaoRedirectUri: String

    fun kakaoLogin(code: String): String {
        // 1) 인가 코드로 액세스 토큰 발급
        val accessToken = kakaoOAuthApiClient.getAccessToken(kakaoClientId, kakaoRedirectUri, code)

        // 2) 사용자 정보 조회
        val kakaoUser = kakaoOAuthApiClient.getKakaoUserInfo(accessToken)

        // 3) DB에서 kakaoId로 조회
        val existingUser = getUserByKakaoIdPort.findByKakaoId(kakaoUser.kakaoId)

        // 4) 없으면 새로 가입 (isStudio=false)
        val user =
            if (existingUser == null) {
                val newUser =
                    User(
                        id = null,
                        loginId = null,
                        password = null,
                        name = kakaoUser.nickname,
                        isStudio = false, // 카카오 OAuth → ROLE_USER
                        createdAt = LocalDateTime.now(),
                        updatedAt = null,
                        kakaoId = kakaoUser.kakaoId,
                    )
                saveUserPort.save(newUser)
            } else {
                existingUser
            }

        // 5) CustomUserDetails로 감싸서 JWT 생성
        val customUserDetails = CustomUserDetails(user)
        return jwtTokenProvider.generateToken(customUserDetails)
    }
}
