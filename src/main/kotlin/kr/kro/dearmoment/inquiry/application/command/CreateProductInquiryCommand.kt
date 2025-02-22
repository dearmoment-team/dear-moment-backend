package kr.kro.dearmoment.inquiry.application.command

import kr.kro.dearmoment.inquiry.domain.ProductInquiry

data class CreateProductInquiryCommand(
    val userId: Long,
    val productId: Long,
) {
    fun toDomain() =
        ProductInquiry(
            userId = userId,
            productId = productId,
        )
}
