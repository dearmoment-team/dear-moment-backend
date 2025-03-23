package kr.kro.dearmoment.inquiry.application.command

data class RemoveProductOptionInquiryCommand(
    val inquiryId: Long,
    val productId: Long,
)
