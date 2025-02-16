package kr.kro.dearmoment.inquiry.adapter.input.web.service.dto

import jakarta.validation.constraints.NotBlank
import org.jetbrains.annotations.NotNull

data class CreateServiceInquiryRequest(
    @field:NotNull(value = "유저 ID 널이 될 수 없습니다.")
    val userId: Long,
    @field:NotBlank(message = "서비스 피드백 타입은 빈 문자열이 될 수 없습니다.")
    val type: String,
    @field:NotBlank(message = "내용은 빈 문자열이 될 수 없습니다.")
    val content: String,
)
