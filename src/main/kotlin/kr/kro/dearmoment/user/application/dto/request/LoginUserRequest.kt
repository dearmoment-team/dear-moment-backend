package kr.kro.dearmoment.user.application.dto.request

// 임시 작가 로그인
data class LoginUserRequest(
    val loginId: String,
    val password: String,
)
