package kr.kro.dearmoment.product.application.port.out

import kr.kro.dearmoment.product.domain.model.Product

interface ProductPersistencePort {
    fun save(product: Product): Product

    fun findById(id: Long): Product?

    fun findAll(): List<Product>
}
