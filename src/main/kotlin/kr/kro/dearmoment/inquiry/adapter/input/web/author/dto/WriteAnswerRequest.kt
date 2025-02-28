package kr.kro.dearmoment.inquiry.adapter.input.web.author.dto

import jakarta.validation.constraints.NotBlank

data class WriteAnswerRequest(
    @field:NotBlank(message = "내용은 필수 입니다.")
    val answer: String,
)
