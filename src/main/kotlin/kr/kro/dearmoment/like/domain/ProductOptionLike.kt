package kr.kro.dearmoment.like.domain

import kr.kro.dearmoment.product.domain.model.Product

class ProductOptionLike(
    id: Long = 0L,
    userId: Long,
    val studioName: String,
    val productOptionId: Long,
    val product: Product,
) : Like(id, userId) {
    init {
        require(product.productId > 0) { "상품 ID는 양수이어야 합니다." }
        require(productOptionId > 0) { "상품 옵션ID는 양수이어야 합니다." }
    }
}
