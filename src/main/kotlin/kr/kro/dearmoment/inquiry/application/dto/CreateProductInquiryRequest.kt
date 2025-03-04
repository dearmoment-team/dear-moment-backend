package kr.kro.dearmoment.inquiry.application.dto

import org.jetbrains.annotations.NotNull

data class CreateProductInquiryRequest(
    @field:NotNull(value = "유저 ID 널이 될 수 없습니다.")
    val userId: Long,
    @field:NotNull(value = "상품 ID 널이 될 수 없습니다.")
    val productId: Long,
)
