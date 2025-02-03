package kr.kro.dearmoment.product.domain.model

data class ProductOptionUpdateResult(
    val updatedOptions: List<ProductOption>,
    val removedOptionIds: Set<Long>,
)
