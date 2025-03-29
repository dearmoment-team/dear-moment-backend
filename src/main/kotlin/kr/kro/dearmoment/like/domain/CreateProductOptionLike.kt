package kr.kro.dearmoment.like.domain

import java.util.UUID

class CreateProductOptionLike(
    id: Long = 0L,
    userId: UUID,
    val productOptionId: Long,
) : Like(id, userId) {
    init {
        require(productOptionId > 0) { "상품 ID는 양수이어야 합니다." }
    }
}
