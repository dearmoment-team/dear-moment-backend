package kr.kro.dearmoment.inquiry.application.command

data class DeleteInquiryCommand(
    val inquiryId: Long,
    val type: String,
)
