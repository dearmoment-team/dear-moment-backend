package kr.kro.dearmoment.user.application.dto.response

data class LoginUserResponse(
    val success: Boolean,
    val token: String,
)
