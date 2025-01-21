package kr.kro.dearmoment.product.adapter.persistence

import kr.kro.dearmoment.product.application.port.ProductRepository
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryAdapter(
    private val jpaProductRepository: JpaProductRepository,
) : ProductRepository {
    override fun save(product: Product): Product {
        val entity = ProductEntity.fromDomain(product)
        val saved = jpaProductRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: Long): Product? {
        val entity = jpaProductRepository.findById(id).orElse(null)
        return entity?.toDomain()
    }

    override fun findAll(): List<Product> {
        return jpaProductRepository.findAll()
            .map { it.toDomain() }
    }
}

interface JpaProductRepository : JpaRepository<ProductEntity, Long>
