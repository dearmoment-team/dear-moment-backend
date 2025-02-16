package kr.kro.dearmoment.inquiry.adapter.input.web.service.dto

data class GetServiceInquiryResponse(
    val inquiryId: Long,
    val type: String,
    val content: String,
)
