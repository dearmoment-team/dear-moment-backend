package kr.kro.dearmoment.product.domain.sort

import kr.kro.dearmoment.product.domain.model.Product

class PriceLowSortStrategy : SortStrategy {
    override fun sort(products: List<Product>): List<Product> =
        products.sortedBy { product -> product.options.minOf { option -> option.originalPrice } }
}
