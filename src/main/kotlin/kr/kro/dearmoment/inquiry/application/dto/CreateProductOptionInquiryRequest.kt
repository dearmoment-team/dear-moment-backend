package kr.kro.dearmoment.inquiry.application.dto

import kr.kro.dearmoment.inquiry.application.command.CreateProductInquiryCommand
import org.jetbrains.annotations.NotNull

data class CreateProductOptionInquiryRequest(
    @field:NotNull(value = "유저 ID 널이 될 수 없습니다.")
    val userId: Long,
    @field:NotNull(value = "상품 옵션 ID 널이 될 수 없습니다.")
    val productOptionId: Long,
) {
    fun toCommand() =
        CreateProductInquiryCommand(
            userId = userId,
            productId = productOptionId,
        )
}
