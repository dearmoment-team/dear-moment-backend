package kr.kro.dearmoment.common.constants

class GlobalUrls {
    companion object {
        // API 기본 경로
        const val API = "/api"

        // User 관련 경로
        const val API_USERS = "$API/users"
        const val API_USERS_SIGNUP = "$API_USERS/signup"
        const val API_USERS_LOGIN = "$API_USERS/login"

        // OAuth 관련 경로
        const val OAUTH = "/oauth"
        const val OAUTH_KAKAO = "$OAUTH/kakao"
        const val OAUTH_KAKAO_CALLBACK = "$OAUTH_KAKAO/callback"
        const val OAUTH_KAKAO_REDIRECT = "$OAUTH_KAKAO/redirect"
        const val OAUTH_KAKAO_WITHDRAW = "$OAUTH_KAKAO/withdraw"
    }
}
