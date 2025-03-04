package kr.kro.dearmoment.inquiry.application.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.jetbrains.annotations.NotNull

data class CreateServiceInquiryRequest(
    @field:NotNull(value = "유저 ID 널이 될 수 없습니다.")
    val userId: Long,
    @field:NotBlank(message = "서비스 피드백 타입은 빈 문자열이 될 수 없습니다.")
    val type: String,
    @field:NotBlank(message = "내용은 빈 문자열이 될 수 없습니다.")
    @field:Min(value = 10, message = "내용은 최소 10자 이상이어야 합니다.")
    @field:Max(value = 1000, message = "내용은 최소 1000자 이하이어야 합니다.")
    val content: String,
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,
)
