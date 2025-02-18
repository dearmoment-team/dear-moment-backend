package kr.kro.dearmoment.inquiry.application.port.output

import kr.kro.dearmoment.inquiry.domain.AuthorInquiry
import kr.kro.dearmoment.inquiry.domain.ProductInquiry

interface GetInquiryPort {
    fun getAuthorInquiries(userId: Long): List<AuthorInquiry>

    fun getProductInquiries(userId: Long): List<ProductInquiry>
}
