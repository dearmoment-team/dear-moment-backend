package kr.kro.dearmoment.inquiry.adapter.output.mail.event

data class InquiryCreateEvent(
    val email: String,
    val subject: String,
    val body: String,
)
