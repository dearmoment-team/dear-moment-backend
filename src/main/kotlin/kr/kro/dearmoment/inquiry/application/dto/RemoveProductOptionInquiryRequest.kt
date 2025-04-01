package kr.kro.dearmoment.inquiry.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.inquiry.application.command.RemoveProductOptionInquiryCommand
import org.jetbrains.annotations.NotNull
import java.util.UUID

data class RemoveProductOptionInquiryRequest(
    @Schema(description = "삭제할 문의 ID", example = "1", required = true)
    @field:NotNull(value = "삭제할 문의 ID는 널이 될 수 없습니다.")
    val inquiryId: Long,
    @Schema(description = "상품 ID", example = "1", required = true)
    @field:NotNull(value = "상품 ID는 널이 될 수 없습니다.")
    val productId: Long,
) {
    fun toCommand(userId: UUID) =
        RemoveProductOptionInquiryCommand(
            userId = userId,
            inquiryId = inquiryId,
            productId = productId,
        )
}
