package kr.kro.dearmoment.inquiry.application.port.output

interface SendInquiryPort {
    fun sendMail(
        email: String,
        subject: String,
        body: String,
    )
}
