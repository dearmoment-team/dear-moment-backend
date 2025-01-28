package kr.kro.dearmoment.product.application.port.out

import kr.kro.dearmoment.product.domain.model.Product

interface ProductPersistencePort {

    fun save(product: Product): Product

    fun findById(id: Long): Product?

    fun findAll(): List<Product>

    fun findByUserId(userId: Long): List<Product>

    fun existsById(id: Long): Boolean

    fun searchByCriteria(title: String?, priceRange: Pair<Long?, Long?>?): List<Product>

    fun deleteById(id: Long)
}
