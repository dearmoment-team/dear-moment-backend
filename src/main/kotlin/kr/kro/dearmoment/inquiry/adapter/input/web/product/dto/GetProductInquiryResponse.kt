package kr.kro.dearmoment.inquiry.adapter.input.web.product.dto

import kr.kro.dearmoment.inquiry.domain.ProductInquiry
import java.time.LocalDateTime

data class GetProductInquiryResponse(
    val inquiryId: Long,
    val productId: Long,
    val thumbnailUrl: String,
    val createdDate: LocalDateTime,
) {
    companion object {
        fun from(inquiry: ProductInquiry) =
            GetProductInquiryResponse(
                inquiryId = inquiry.id,
                productId = inquiry.productId,
                thumbnailUrl = inquiry.thumbnailUrl,
                createdDate = inquiry.createdDate,
            )
    }
}
