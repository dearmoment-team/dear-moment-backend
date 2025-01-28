package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ProductOptionRepositoryAdapter(
    private val jpaProductOptionRepository: JpaProductOptionRepository,
) : ProductOptionPersistencePort {
    @Transactional
    override fun save(
        productOption: ProductOption,
        productEntity: ProductEntity,
    ): ProductOption {
        if (jpaProductOptionRepository.existsByProductProductIdAndName(productEntity.productId!!, productOption.name)) {
            throw IllegalArgumentException("ProductOption already exists: ${productOption.name}")
        }

        val optionEntity = ProductOptionEntity.fromDomain(productOption, productEntity)
        val savedEntity = jpaProductOptionRepository.save(optionEntity)

        return savedEntity.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): ProductOption {
        return jpaProductOptionRepository.findById(id)
            .orElseThrow { IllegalArgumentException("ProductOption with ID $id not found") }
            .toDomain()
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<ProductOption> {
        return jpaProductOptionRepository.findAll().map { it.toDomain() }
    }

    @Transactional
    override fun deleteById(id: Long) {
        if (!jpaProductOptionRepository.existsById(id)) {
            throw IllegalArgumentException("Cannot delete ProductOption: ID $id not found.")
        }
        jpaProductOptionRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    override fun findByProduct(product: Product): List<ProductOption> {
        val productId =
            product.productId
                ?: throw IllegalArgumentException("Product ID must be provided for finding options")

        return jpaProductOptionRepository.findByProductProductId(productId).map { it.toDomain() }
    }

    @Transactional
    override fun deleteAllByProductId(productId: Long) {
        jpaProductOptionRepository.deleteAllByProductProductId(productId)
    }

    @Transactional(readOnly = true)
    override fun existsByProductId(productId: Long): Boolean {
        return jpaProductOptionRepository.existsByProductProductId(productId)
    }
}
