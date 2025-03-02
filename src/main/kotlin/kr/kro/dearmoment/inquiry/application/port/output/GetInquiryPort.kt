package kr.kro.dearmoment.inquiry.application.port.output

import kr.kro.dearmoment.inquiry.domain.AuthorInquiry
import kr.kro.dearmoment.inquiry.domain.ProductInquiry
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetInquiryPort {
    fun getAuthorInquiries(
        userId: Long,
        pageable: Pageable,
    ): Page<AuthorInquiry>

    fun getProductInquiries(
        userId: Long,
        pageable: Pageable,
    ): Page<ProductInquiry>
}
