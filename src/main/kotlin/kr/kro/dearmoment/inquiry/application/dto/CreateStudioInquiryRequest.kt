package kr.kro.dearmoment.inquiry.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import kr.kro.dearmoment.inquiry.application.command.CreateStudioInquiryCommand
import java.util.UUID

data class CreateStudioInquiryRequest(
    @Schema(description = "제목 (최대 15자)", example = "스튜디오 정보가 잘못되었습니다. ", required = true)
    @field:NotBlank(message = "제목은 빈 문자열이 될 수 없습니다.")
    @field:Max(value = 15L, message = "제목은 최대 15자 이하여야합니다.")
    val title: String,
    @Schema(description = "내용 (200자 이하)", example = "서비스 문의 내용", required = true)
    @field:NotBlank(message = "내용은 빈 문자열이 될 수 없습니다.")
    @field:Max(value = 200L, message = "내용은 최대 200자 이하여야합니다.")
    val content: String,
    @Schema(description = "답변 답을 이메일", example = "email@gmail.com", required = true)
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,
) {
    fun toCommand(userId: UUID) =
        CreateStudioInquiryCommand(
            userId = userId,
            title = title,
            content = content,
            email = email,
        )
}
