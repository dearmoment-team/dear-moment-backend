package kr.kro.dearmoment.product.domain.model

import java.time.LocalDateTime

data class ProductOption(
    val optionId: Long = 0L,
    val name: String,
    val additionalPrice: Long,
    val description: String? = null,
    val productId: Long = 0L,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
