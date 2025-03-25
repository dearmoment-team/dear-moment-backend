package kr.kro.dearmoment.user.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "사용자 이름 변경 요청 DTO")
data class UpdateUserNameRequest(
    @Schema(description = "새로 변경할 이름", example = "김작가", required = true)
    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,
)
