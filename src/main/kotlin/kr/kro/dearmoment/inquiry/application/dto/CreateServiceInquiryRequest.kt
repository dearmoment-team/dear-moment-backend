package kr.kro.dearmoment.inquiry.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kr.kro.dearmoment.common.validation.EnumValue
import kr.kro.dearmoment.inquiry.application.command.CreateServiceInquiryCommand
import kr.kro.dearmoment.inquiry.domain.ServiceInquiryType
import java.util.UUID

data class CreateServiceInquiryRequest(
    @Schema(
        description = "서비스 피드백 타입",
        example = "SERVICE_COMPLIMENT",
        allowableValues = ["SYSTEM_IMPROVEMENT", "SERVICE_SUGGESTION, SYSTEM_IMPROVEMENT", "SYSTEM_IMPROVEMENT"],
    )
    @field:NotBlank(message = "서비스 피드백 타입은 빈 문자열이 될 수 없습니다.")
    @field:EnumValue(enumClass = ServiceInquiryType::class, message = "유효하지 않은 서비스 피드백 타입입니다.")
    val type: String,
    @Schema(description = "내용(10자 이상 1000자 이하)", example = "서비스 문의 내용은 최소 10자 이상이어야 합니다.", required = true)
    @field:NotBlank(message = "내용은 빈 문자열이 될 수 없습니다.")
    @field:Size(min = 10, max = 1000, message = "내용의 길이는 10자 ~ 1000자 이어야 합니다.")
    val content: String,
    @Schema(description = "답변 답을 이메일", example = "email@gmail.com", required = true)
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,
) {
    fun toCommand(userId: UUID) =
        CreateServiceInquiryCommand(
            userId = userId,
            type = type,
            content = content,
            email = email,
        )
}
