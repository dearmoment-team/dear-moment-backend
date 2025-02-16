package kr.kro.dearmoment.inquiry.application.port.output

import kr.kro.dearmoment.inquiry.domain.AuthorInquiry
import kr.kro.dearmoment.inquiry.domain.ProductInquiry
import kr.kro.dearmoment.inquiry.domain.ServiceInquiry

interface SaveInquiryPort {
    fun saveProductInquiry(inquiry: ProductInquiry): Long

    fun saveAuthorInquiry(inquiry: AuthorInquiry): Long

    fun saveServiceInquiry(inquiry: ServiceInquiry): Long
}
