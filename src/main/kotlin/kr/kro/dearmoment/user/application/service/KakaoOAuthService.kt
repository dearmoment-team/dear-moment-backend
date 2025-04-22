package kr.kro.dearmoment.user.application.service

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.security.JwtTokenProvider
import kr.kro.dearmoment.user.adapter.output.oauth.KakaoOAuthApiClient
import kr.kro.dearmoment.user.adapter.output.persistence.UserPersistenceAdapter
import kr.kro.dearmoment.user.application.port.output.GetUserByIdPort
import kr.kro.dearmoment.user.application.port.output.GetUserByKakaoIdPort
import kr.kro.dearmoment.user.application.port.output.SaveUserPort
import kr.kro.dearmoment.user.domain.User
import kr.kro.dearmoment.user.security.CustomUserDetails
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

/**
 * 카카오 인증/로그인/콜백 처리
 */
@Service
class KakaoOAuthService(
    private val kakaoOAuthApiClient: KakaoOAuthApiClient,
    private val getUserByKakaoIdPort: GetUserByKakaoIdPort,
    private val saveUserPort: SaveUserPort,
    private val getUserByIdPort: GetUserByIdPort,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userPersistenceAdapter: UserPersistenceAdapter,
) {
    @Value("\${oauth.kakao.client-id}")
    lateinit var kakaoClientId: String

    @Value("\${oauth.kakao.redirect-uri}")
    lateinit var kakaoRedirectUri: String

    @Value("\${oauth.kakao.app-admin-key}")
    lateinit var kakaoAppAdminKey: String

    /**
     * 카카오 인가 코드를 이용해 로그인 처리 (JWT 발급)
     */
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

    /**
     * Admin Key 기반으로 unlink:
     * 1) JWT로 식별된 userUuid -> DB에서 찾기
     * 2) 해당 user 의 kakaoId 확인
     * 3) kakaoId로 unlinkAndGetKakaoIdByAdminKey(kakaoId) 호출
     * 4) 응답 kakaoId가 같으면 DB 하드 딜리트
     */
    fun unlinkAndWithdraw(userUuid: UUID) {
        // 1) DB 조회
        val user =
            getUserByIdPort.findById(userUuid)
                ?: throw IllegalArgumentException("No user found with UUID=$userUuid")

        val userKakaoId =
            user.kakaoId
                ?: throw IllegalStateException("User does not have a kakaoId, can't unlink")

        // 2) 카카오 AdminKey 로 unlink
        val responseKakaoId =
            kakaoOAuthApiClient.unlinkAndGetKakaoIdByAdminKey(kakaoAppAdminKey, userKakaoId)
                ?: throw IllegalArgumentException("Unlink failed, no 'id' in response from Kakao")

        if (responseKakaoId != userKakaoId) {
            throw IllegalStateException("Mismatch: user.kakaoId=$userKakaoId vs. $responseKakaoId")
        }

        // 3) 하드 딜리트
        userPersistenceAdapter.deleteUser(userUuid)
    }
}
