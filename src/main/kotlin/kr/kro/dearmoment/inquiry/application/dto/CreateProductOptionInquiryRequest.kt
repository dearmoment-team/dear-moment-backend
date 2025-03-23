package kr.kro.dearmoment.inquiry.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.inquiry.application.command.CreateProductOptionInquiryCommand
import org.jetbrains.annotations.NotNull

data class CreateProductOptionInquiryRequest(
    @Schema(description = "유저 ID", example = "1", required = true)
    @field:NotNull(value = "유저 ID 널이 될 수 없습니다.")
    val userId: Long,
    @Schema(description = "상품 ID", example = "1", required = true)
    @field:NotNull(value = "상품 ID 널이 될 수 없습니다.")
    val productId: Long,
    @Schema(description = "상품 옵션 ID", example = "1", required = true)
    @field:NotNull(value = "상품 옵션 ID 널이 될 수 없습니다.")
    val optionId: Long,
) {
    fun toCommand() =
        CreateProductOptionInquiryCommand(
            userId = userId,
            productId = productId,
            optionId = optionId,
        )
}
