package kr.kro.dearmoment.user.application.dto.request

data class RegisterUserRequest(
    val loginId: String,
    val password: String,
    val name: String
)
