package kr.kro.dearmoment.inquiry.application.command

import kr.kro.dearmoment.inquiry.domain.CreateProductOptionInquiry

data class CreateProductInquiryCommand(
    val userId: Long,
    val productId: Long,
) {
    fun toDomain() =
        CreateProductOptionInquiry(
            userId = userId,
            productOptionId = productId,
        )
}
