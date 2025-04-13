package kr.kro.dearmoment.like.domain

import kr.kro.dearmoment.product.domain.model.Product

enum class SortCriteria {
    RECOMMENDED,
    POPULAR,
    PRICE_LOW,
    PRICE_HIGH,
    ;

    fun toProductOptionLikeComparator(): Comparator<ProductOptionLike> =
        when (this) {
            PRICE_LOW ->
                compareBy {
                    it.product.options
                        .firstOrNull { option -> option.optionId == it.productOptionId }?.discountPrice
                }
            PRICE_HIGH ->
                compareByDescending {
                    it.product.options
                        .firstOrNull { option -> option.optionId == it.productOptionId }?.discountPrice
                }
            POPULAR, RECOMMENDED -> compareByDescending { calculateScore(it.product) }
        }

    fun toProductLikeComparator(): Comparator<ProductLike> =
        when (this) {
            PRICE_LOW -> compareBy { like -> like.product.options.minOf { it.discountPrice } }
            PRICE_HIGH -> compareByDescending { like -> like.product.options.maxOf { it.discountPrice } }
            POPULAR, RECOMMENDED -> compareByDescending { calculateScore(it.product) }
        }

    private fun calculateScore(p: Product): Long =
        when (this) {
            RECOMMENDED -> {
                p.likeCount * PRODUCT_LIKE_COUNT_WEIGHT +
                    p.inquiryCount * INQUIRY_COUNT_WEIGHT +
                    p.optionLikeCount * OPTION_LIKE_COUNT_WEIGHT +
                    if (p.studio?.isCasted == true) CASTED_STUDIO_WEIGHT else 0L
            }
            POPULAR -> {
                p.likeCount * PRODUCT_LIKE_COUNT_WEIGHT +
                    p.inquiryCount * INQUIRY_COUNT_WEIGHT +
                    p.optionLikeCount * OPTION_LIKE_COUNT_WEIGHT
            }
            else -> 0
        }

    companion object {
        private const val PRODUCT_LIKE_COUNT_WEIGHT = 10L
        private const val OPTION_LIKE_COUNT_WEIGHT = 11L
        private const val INQUIRY_COUNT_WEIGHT = 12L
        private const val CASTED_STUDIO_WEIGHT = 11L

        fun from(value: String): SortCriteria =
            SortCriteria.entries.find { it.name == value }
                ?: throw IllegalArgumentException("유효하지 않은 ProductSortCriteria 값: $value")
    }
}
