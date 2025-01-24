package kr.kro.dearmoment.product.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface JpaProductOptionRepository : JpaRepository<ProductOptionEntity, Long> {
    fun findByProduct(product: ProductEntity): List<ProductOptionEntity>
}

