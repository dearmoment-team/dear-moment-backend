package kr.kro.dearmoment.inquiry.application.command

import java.util.UUID

data class RemoveProductOptionInquiryCommand(
    val userId: UUID,
    val inquiryId: Long,
    val productId: Long,
)
