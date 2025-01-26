package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Repository

@Repository
class ProductPersistenceAdapter(
    private val jpaProductRepository: JpaProductRepository
) : ProductPersistencePort {

    override fun save(product: Product): Product {
        val entity = ProductEntity.fromDomain(product)
        return jpaProductRepository.saveAndFlush(entity).toDomain()
    }

    override fun findById(id: Long): Product? {
        return jpaProductRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findAll(): List<Product> {
        return jpaProductRepository.findAll().map { it.toDomain() }
    }

    override fun findByUserId(userId: Long): List<Product> {
        return jpaProductRepository.findByUserId(userId).map { it.toDomain() }
    }

    override fun existsById(id: Long): Boolean {
        return jpaProductRepository.existsById(id)
    }

    override fun searchByCriteria(title: String?, priceRange: Pair<Long?, Long?>?): List<Product> {
        val minPrice = priceRange?.first
        val maxPrice = priceRange?.second

        return jpaProductRepository.searchByCriteria(title, minPrice, maxPrice).map { it.toDomain() }
    }
}
