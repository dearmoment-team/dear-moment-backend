package kr.kro.dearmoment.product.application.usecase.search

enum class ProductSortCriteria {
    RECOMMENDED,
    POPULAR,
    PRICE_LOW,
    PRICE_HIGH,
    ;

    companion object {
        fun from(value: String): ProductSortCriteria =
            ProductSortCriteria.entries.find { it.name == value }
                ?: throw IllegalArgumentException("유효하지 않은 ProductSortCriteria 값: $value")
    }
}
