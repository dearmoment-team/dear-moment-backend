package kr.kro.dearmoment.inquiry.adapter.input.web.author.dto

import jakarta.validation.constraints.NotBlank
import org.jetbrains.annotations.NotNull

data class CreateAuthorInquiryRequest(
    @field:NotNull(value = "유저 ID 널이 될 수 없습니다.")
    val userId: Long,
    @field:NotBlank(message = "제목은 빈 문자열이 될 수 없습니다.")
    val title: String,
    @field:NotBlank(message = "내용은 빈 문자열이 될 수 없습니다.")
    val content: String,
)
