package kr.kro.dearmoment.like.domain

import kr.kro.dearmoment.product.domain.model.Product
import java.util.UUID

class ProductLike(
    id: Long = 0L,
    userId: UUID,
    val product: Product,
) : Like(id, userId) {
    init {
        require(product.productId > 0) { "상품 ID는 양수이어야 합니다." }
    }
}
