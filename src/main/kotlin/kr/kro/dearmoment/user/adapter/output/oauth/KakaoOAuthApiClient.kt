package kr.kro.dearmoment.user.adapter.output.oauth

import kr.kro.dearmoment.user.application.dto.response.KakaoUserInfo
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@Component
class KakaoOAuthApiClient {
    // 카카오 인증 서버
    private val oauthClient =
        WebClient.builder()
            .baseUrl("https://kauth.kakao.com")
            .build()

    // 카카오 사용자 정보
    private val apiClient =
        WebClient.builder()
            .baseUrl("https://kapi.kakao.com")
            .build()

    fun getAccessToken(
        clientId: String,
        redirectUri: String,
        code: String,
    ): String {
        val responseMap =
            oauthClient.post()
                .uri("/oauth/token?grant_type=authorization_code&client_id=$clientId&redirect_uri=$redirectUri&code=$code")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block() ?: throw RuntimeException("Failed to retrieve token from Kakao")

        return responseMap["access_token"]?.toString()
            ?: throw RuntimeException("No access_token found in Kakao response")
    }

    fun getKakaoUserInfo(accessToken: String): KakaoUserInfo {
        val responseMap =
            apiClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block() ?: throw RuntimeException("Failed to retrieve user info from Kakao")

        val id =
            responseMap["id"]?.toString()?.toLongOrNull()
                ?: throw RuntimeException("No kakao user id found")

        // kakao_account.profile.nickname 가져오기
        val kakaoAccount = responseMap["kakao_account"] as? Map<*, *>
        val profile = kakaoAccount?.get("profile") as? Map<*, *>
        val nickname = profile?.get("nickname")?.toString() ?: "카카오사용자"

        return KakaoUserInfo(
            kakaoId = id,
            nickname = nickname,
        )
    }

    /**
     * 3) "관리자 키(Admin Key)" 방식으로 특정 kakaoId를 강제 연결 끊기
     *    - POST /v1/user/unlink
     *    - Content-Type: application/x-www-form-urlencoded
     *    - Authorization: KakaoAK {ADMIN_KEY}
     *    - body: target_id_type=user_id & target_id={kakaoId}
     *
     * 응답 예시:
     *  { "id": 123456789 }
     */
    fun unlinkAndGetKakaoIdByAdminKey(
        adminKey: String,
        kakaoId: Long,
    ): Long? {
        val responseMap =
            apiClient.post()
                .uri("/v1/user/unlink")
                .header("Authorization", "KakaoAK $adminKey")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                    BodyInserters
                        .fromFormData("target_id_type", "user_id")
                        .with("target_id", kakaoId.toString()),
                )
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

        if (responseMap == null) {
            return null
        }
        // 응답 {"id": 123456789} 식
        return responseMap["id"]?.toString()?.toLongOrNull()
    }
}
