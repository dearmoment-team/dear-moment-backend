package kr.kro.dearmoment.inquiry.application.dto

import kr.kro.dearmoment.inquiry.domain.ProductOptionInquiry
import java.time.LocalDateTime

data class GetProductOptionInquiryResponse(
    val inquiryId: Long,
    val studioName: String,
    val optionName: String,
    val thumbnailUrl: String,
    val createdDate: LocalDateTime,
) {
    companion object {
        fun from(inquiry: ProductOptionInquiry) =
            GetProductOptionInquiryResponse(
                inquiryId = inquiry.id,
                studioName = inquiry.studioName,
                optionName = inquiry.optionName,
                thumbnailUrl = inquiry.thumbnailUrl,
                createdDate = inquiry.createdDate,
            )
    }
}
