package kr.kro.dearmoment.product.domain.model

import java.time.LocalDateTime

data class ProductOption(
    var optionId: Long = 0L,
    var productId: Long,
    var name: String,
    var additionalPrice: Long,
    var description: String = "",
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
) {
    init {
        require(name.isNotBlank()) { "옵션명은 비어 있을 수 없습니다." }
        require(additionalPrice >= 0) { "추가 가격은 음수가 될 수 없습니다." }
    }
}
