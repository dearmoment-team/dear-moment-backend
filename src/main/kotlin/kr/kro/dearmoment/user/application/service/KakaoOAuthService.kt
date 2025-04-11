package kr.kro.dearmoment.user.application.service

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
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
        // 1) 인가 코드로 액세스 토큰 발급 시도
        val accessToken =
            try {
                kakaoOAuthApiClient.getAccessToken(kakaoClientId, kakaoRedirectUri, code)
            } catch (e: Exception) {
                throw CustomException(ErrorCode.KAKAO_LOGIN_FAILED)
            }

        // 2) 사용자 정보 조회 시도
        val kakaoUser =
            try {
                kakaoOAuthApiClient.getKakaoUserInfo(accessToken)
            } catch (e: Exception) {
                throw CustomException(ErrorCode.KAKAO_LOGIN_FAILED)
            }

        // 3) DB 에서 kakaoId로 조회 후 회원가입 또는 기존 정보 조회
        val user =
            try {
                getUserByKakaoIdPort.findByKakaoId(kakaoUser.kakaoId) ?: run {
                    val newUser =
                        User(
                            id = null,
                            loginId = null,
                            password = null,
                            name = kakaoUser.nickname,
                            isStudio = false,
                            createdAt = LocalDateTime.now(),
                            updatedAt = null,
                            kakaoId = kakaoUser.kakaoId,
                        )
                    saveUserPort.save(newUser)
                }
                throw CustomException(ErrorCode.DB_SIGNUP_FAILED)
            } catch (e: Exception) {
                throw CustomException(ErrorCode.DB_SIGNUP_FAILED)
            }

        // 4) JWT 생성
        return try {
            val customUserDetails = CustomUserDetails(user)
            jwtTokenProvider.generateToken(customUserDetails)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.OAUTH_SERVER_PROCESS_FAILED)
        }
    }
}
