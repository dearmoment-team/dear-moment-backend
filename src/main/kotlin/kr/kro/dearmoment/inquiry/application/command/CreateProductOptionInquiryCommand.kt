package kr.kro.dearmoment.inquiry.application.command

import kr.kro.dearmoment.inquiry.domain.CreateProductOptionInquiry

data class CreateProductOptionInquiryCommand(
    val userId: Long,
    val productId: Long,
    val optionId: Long,
) {
    fun toDomain() =
        CreateProductOptionInquiry(
            userId = userId,
            productId = productId,
            optionId = optionId,
        )
}
