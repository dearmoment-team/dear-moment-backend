package kr.kro.dearmoment.product.application.port.out

import kr.kro.dearmoment.product.domain.model.ProductOption

interface ProductOptionPersistencePort {
    fun save(productOption: ProductOption): ProductOption
    fun findById(id: Long): ProductOption
    fun findAll(): List<ProductOption>
    fun deleteById(id: Long)
    fun findByProductId(productId: Long): List<ProductOption>
}
