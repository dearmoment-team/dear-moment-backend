package kr.kro.dearmoment.user.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "회원가입 요청 DTO (임시 작가용)")
data class RegisterUserRequest(
    @Schema(description = "로그인 ID(이메일 또는 임시 아이디)", example = "artist123", required = true)
    @field:NotBlank(message = "로그인 ID는 필수입니다.")
    val loginId: String,
    @Schema(description = "비밀번호", example = "password!23", required = true)
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,
    @Schema(description = "사용자 이름(추후 제거 예정)", example = "홍길동", required = true)
    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,
)
