package kr.kro.dearmoment.inquiry.application.port.output

interface DeleteInquiryPort {
    fun deleteProductInquiry(inquiryId: Long)

    fun deleteAuthorInquiry(inquiryId: Long)

    fun deleteServiceInquiry(inquiryId: Long)
}
