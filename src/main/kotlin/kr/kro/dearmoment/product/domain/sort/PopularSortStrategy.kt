package kr.kro.dearmoment.product.domain.sort

import kr.kro.dearmoment.product.domain.model.Product

class PopularSortStrategy : SortStrategy {
    override fun sort(products: List<Product>): List<Product> {
        return products.sortedByDescending { product ->
            val productLikeCount = product.likeCount
            val productOptionsLikeCount = product.options.sumOf { it.likeCount }
            val productInquiryCount = product.inquiryCount

            (productLikeCount * PRODUCT_LIKE_COUNT_WEIGHT) +
                (productOptionsLikeCount * PRODUCT_OPTION_LIKE_COUNT_WEIGHT) +
                (productInquiryCount * PRODUCT_INQUIRY_COUNT_WEIGHT)
        }
    }

    companion object {
        private const val PRODUCT_LIKE_COUNT_WEIGHT = 1.0
        private const val PRODUCT_OPTION_LIKE_COUNT_WEIGHT = 1.1
        private const val PRODUCT_INQUIRY_COUNT_WEIGHT = 1.2
    }
}
