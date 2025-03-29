package kr.kro.dearmoment.inquiry.application.command

import kr.kro.dearmoment.inquiry.domain.CreateProductOptionInquiry
import java.util.UUID

data class CreateProductOptionInquiryCommand(
    val userId: UUID,
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
