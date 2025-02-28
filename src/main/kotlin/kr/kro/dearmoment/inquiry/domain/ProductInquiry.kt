package kr.kro.dearmoment.inquiry.domain

import java.time.LocalDateTime

class ProductInquiry(
    id: Long = 0L,
    userId: Long = 0L,
    val productId: Long,
    val thumbnailUrl: String = "",
    val createdDate: LocalDateTime = LocalDateTime.now(),
) : Inquiry(id, userId) {
    init {
        require(productId > 0) { "상품 ID는 양수여야 합니다." }
    }
}
