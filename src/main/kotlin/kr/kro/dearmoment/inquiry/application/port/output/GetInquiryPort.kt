package kr.kro.dearmoment.inquiry.application.port.output

import kr.kro.dearmoment.inquiry.domain.ProductOptionInquiry
import kr.kro.dearmoment.inquiry.domain.StudioInquiry
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface GetInquiryPort {
    fun findUserStudioInquiries(
        userId: UUID,
        pageable: Pageable,
    ): Page<StudioInquiry>

    fun findUserProductOptionInquiries(
        userId: UUID,
        pageable: Pageable,
    ): Page<ProductOptionInquiry>
}
