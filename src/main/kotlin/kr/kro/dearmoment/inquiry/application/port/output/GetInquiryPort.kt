package kr.kro.dearmoment.inquiry.application.port.output

import kr.kro.dearmoment.inquiry.domain.ArtistInquiry
import kr.kro.dearmoment.inquiry.domain.ProductInquiry
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetInquiryPort {
    fun getArtistInquiries(
        userId: Long,
        pageable: Pageable,
    ): Page<ArtistInquiry>

    fun getProductInquiries(
        userId: Long,
        pageable: Pageable,
    ): Page<ProductInquiry>
}
