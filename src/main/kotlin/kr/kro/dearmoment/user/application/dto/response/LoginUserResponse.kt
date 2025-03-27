package kr.kro.dearmoment.user.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class LoginUserResponse(
    @Schema(description = "로그인 성공 여부", example = "true")
    val success: Boolean,
    @Schema(description = "JWT 토큰(로그인 성공 시 발급)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    val token: String,
)
