package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.stereotype.Repository

@Repository
class ProductOptionRepositoryAdapter(
    private val jpaProductOptionRepository: JpaProductOptionRepository,
    private val productEntityRetrievalPort: ProductEntityRetrievalPort,
) : ProductOptionPersistencePort {

    override fun save(productOption: ProductOption): ProductOption {
        val product = getProduct(productOption.productId)
        val productEntity = ProductEntity.fromDomain(product)

        val entity = ProductOptionEntity.fromDomain(productOption, productEntity)

        val savedEntity = jpaProductOptionRepository.save(entity)

        return savedEntity.toDomain()
    }

    override fun findById(id: Long): ProductOption {
        return jpaProductOptionRepository.findById(id)
            .orElseThrow { IllegalArgumentException("ProductOption with ID $id not found") }
            .toDomain()
    }

    override fun findAll(): List<ProductOption> {
        return jpaProductOptionRepository.findAll().map { it.toDomain() }
    }

    override fun deleteById(id: Long) {
        if (!jpaProductOptionRepository.existsById(id)) {
            throw IllegalArgumentException("Cannot delete ProductOption: ID $id does not exist.")
        }
        jpaProductOptionRepository.deleteById(id)
    }

    override fun findByProduct(product: Product): List<ProductOption> {
        val productEntity = ProductEntity.fromDomain(product)
        return jpaProductOptionRepository.findByProduct(productEntity).map { it.toDomain() }
    }

    private fun getProduct(productId: Long): Product {
        return productEntityRetrievalPort.getProductById(productId)
    }
}
