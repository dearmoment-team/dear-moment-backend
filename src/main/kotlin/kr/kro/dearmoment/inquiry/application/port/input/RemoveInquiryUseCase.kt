package kr.kro.dearmoment.inquiry.application.port.input

interface RemoveInquiryUseCase {
    fun removeProductInquiry(inquiryId: Long)

    fun removeAuthorInquiry(inquiryId: Long)

    fun removeServiceInquiry(inquiryId: Long)
}
