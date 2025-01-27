package kr.kro.dearmoment.product.domain.model

import java.time.LocalDateTime

data class ProductOption(
    val optionId: Long? = null,
    val productId: Long? = null,
    val name: String,
    val additionalPrice: Long,
    val description: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)