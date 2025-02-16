package kr.kro.dearmoment.inquiry.application.command

import kr.kro.dearmoment.inquiry.domain.ProductInquiry

data class CreateProductInquiryCommand(
    val userId: Long,
    val productId: Long,
) {
    companion object {
        fun toDomain(command: CreateProductInquiryCommand) =
            ProductInquiry(
                userId = command.userId,
                productId = command.productId,
            )
    }
}
