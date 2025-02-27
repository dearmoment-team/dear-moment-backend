package kr.kro.dearmoment.inquiry.application.command

data class WriteAuthorInquiryAnswerCommand(
    val inquiryId: Long,
    val answer: String,
)
