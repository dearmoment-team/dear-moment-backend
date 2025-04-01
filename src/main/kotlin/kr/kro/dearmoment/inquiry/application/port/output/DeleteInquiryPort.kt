package kr.kro.dearmoment.inquiry.application.port.output

import java.util.UUID

interface DeleteInquiryPort {
    fun deleteProductOptionInquiry(
        inquiryId: Long,
        userId: UUID,
    )
}
