package kr.kro.dearmoment.product.domain.sort

import kr.kro.dearmoment.product.domain.model.Product

class PriceHighSortStrategy : SortStrategy {
    override fun sort(products: List<Product>): List<Product> =
        products.sortedByDescending { product -> product.options.maxOf { option -> option.originalPrice } }
}
