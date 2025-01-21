package kr.kro.dearmoment.product.domain.model

data class Wishlist(
    val id: Long = 0L,
    val userId: Long,
    val productId: Long,
)
