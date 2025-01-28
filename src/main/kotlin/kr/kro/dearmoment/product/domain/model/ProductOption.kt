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
) {
    init {
        require(name.isNotBlank()) { "옵션명은 비어 있을 수 없습니다." }
        require(additionalPrice >= 0) { "추가 가격은 음수가 될 수 없습니다." }
    }
}