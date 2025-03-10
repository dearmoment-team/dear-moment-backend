package kr.kro.dearmoment.inquiry.application.dto

import io.swagger.v3.oas.annotations.media.Schema

class CreateInquiryResponse(
    @Schema(description = "문의 ID", example = "1")
    val inquiryId: Long,
)
