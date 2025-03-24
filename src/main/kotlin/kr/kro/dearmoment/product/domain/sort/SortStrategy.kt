package kr.kro.dearmoment.product.domain.sort

import kr.kro.dearmoment.product.domain.model.Product

interface SortStrategy {
    fun sort(products: List<Product>): List<Product>
}
