package kr.kro.dearmoment.inquiry.domain

import java.util.UUID

class CreateProductOptionInquiry(
    id: Long = 0L,
    userId: UUID,
    val productId: Long,
    val optionId: Long,
) : Inquiry(id, userId) {
    init {
        require(productId > 0) { "상품 옵션 ID는 양수여야 합니다." }
        require(optionId > 0) { "상품 옵션 ID는 양수여야 합니다." }
    }
}
