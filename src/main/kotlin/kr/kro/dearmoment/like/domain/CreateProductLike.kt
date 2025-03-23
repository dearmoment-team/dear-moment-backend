package kr.kro.dearmoment.like.domain

class CreateProductLike(
    id: Long = 0L,
    userId: Long,
    val productId: Long,
) : Like(id, userId) {
    init {
        require(productId > 0) { "상품 ID는 양수이어야 합니다." }
    }
}
