package kr.kro.dearmoment.inquiry.application.port.output

interface UpdateInquiryPort {
    fun updateAuthorInquiryAnswer(
        inquiryId: Long,
        answer: String,
    ): Long
}
