package kr.kro.dearmoment.product.domain.sort

enum class SortCriteria(
    val strategy: SortStrategy,
) {
    RECOMMENDED(RecommendSortStrategy()),
    POPULAR(PopularSortStrategy()),
    PRICE_LOW(PriceLowSortStrategy()),
    PRICE_HIGH(PriceHighSortStrategy()),
    ;

    companion object {
        fun from(value: String): SortCriteria =
            SortCriteria.entries.find { it.name == value }
                ?: throw IllegalArgumentException("유효하지 않은 ProductSortCriteria 값: $value")
    }
}
