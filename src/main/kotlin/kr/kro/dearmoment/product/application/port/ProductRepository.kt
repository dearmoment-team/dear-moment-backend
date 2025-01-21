package kr.kro.dearmoment.product.application.port

import kr.kro.dearmoment.product.domain.model.Product

interface ProductRepository {
    fun save(product: Product): Product

    fun findById(id: Long): Product?

    fun findAll(): List<Product>
}
