package kr.kro.dearmoment.inquiry.application.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import kr.kro.dearmoment.inquiry.application.command.CreateStudioInquiryCommand
import org.jetbrains.annotations.NotNull

data class CreateStudioInquiryRequest(
    @field:NotNull(value = "유저 ID 널이 될 수 없습니다.")
    val userId: Long,
    @field:NotBlank(message = "제목은 빈 문자열이 될 수 없습니다.")
    @field:Max(value = 15L, message = "제목은 최대 15자 이하여야합니다.")
    val title: String,
    @field:NotBlank(message = "내용은 빈 문자열이 될 수 없습니다.")
    @field:Max(value = 200L, message = "내용은 최대 200자 이하여야합니다.")
    val content: String,
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,
) {
    fun toCommand() =
        CreateStudioInquiryCommand(
            userId = userId,
            title = title,
            content = content,
            email = email,
        )
}
