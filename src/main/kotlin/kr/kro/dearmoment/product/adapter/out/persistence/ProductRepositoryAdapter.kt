package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Repository

@Repository
class ProductRepositoryAdapter(
    private val jpaProductRepository: JpaProductRepository,
) : ProductPersistencePort, ProductEntityRetrievalPort {
    override fun save(product: Product): Product {
        val entity = ProductEntity.fromDomain(product)
        val saved = jpaProductRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: Long): Product {
        return jpaProductRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product with ID $id not found") }
            .toDomain()
    }

    override fun findAll(): List<Product> {
        return jpaProductRepository.findAll()
            .map { it.toDomain() }
    }

    override fun getProductEntityById(id: Long): ProductEntity {
        return jpaProductRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product with ID $id not found") }
    }
}
