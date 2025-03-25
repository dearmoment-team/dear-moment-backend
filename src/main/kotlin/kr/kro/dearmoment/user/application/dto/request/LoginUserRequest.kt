package kr.kro.dearmoment.user.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "로그인 요청 DTO (임시 작가 로그인용)")
data class LoginUserRequest(
    @Schema(description = "로그인 ID(이메일 또는 임시 아이디)", example = "artist123", required = true)
    @field:NotBlank(message = "로그인 ID는 필수입니다.")
    val loginId: String,
    @Schema(description = "비밀번호", example = "password!23", required = true)
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,
)
