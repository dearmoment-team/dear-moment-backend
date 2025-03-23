package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ProductOptionRepositoryAdapter(
    private val jpaProductOptionRepository: JpaProductOptionRepository,
    private val jpaProductRepository: JpaProductRepository,
) : ProductOptionPersistencePort {

    @Transactional
    override fun save(
        productOption: ProductOption,
        product: Product,
    ): ProductOption {
        val productEntity =
            jpaProductRepository.findById(product.productId)
                .orElseThrow { CustomException(ErrorCode.PRODUCT_NOT_FOUND) }

        if (jpaProductOptionRepository.existsByProductProductIdAndName(
                productEntity.productId!!,
                productOption.name,
            )
        ) {
            throw CustomException(ErrorCode.DUPLICATE_OPTION_NAME)
        }

        val optionEntity = ProductOptionEntity.fromDomain(productOption, productEntity)
        val savedEntity = jpaProductOptionRepository.save(optionEntity)
        return savedEntity.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): ProductOption {
        return jpaProductOptionRepository.findById(id)
            .orElseThrow { CustomException(ErrorCode.OPTION_NOT_FOUND) }
            .toDomain()
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<ProductOption> {
        return jpaProductOptionRepository.findAll().map { it.toDomain() }
    }

    @Transactional
    override fun deleteById(id: Long) {
        if (!jpaProductOptionRepository.existsById(id)) {
            throw CustomException(ErrorCode.OPTION_NOT_FOUND)
        }
        jpaProductOptionRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    override fun findByProductId(productId: Long): List<ProductOption> {
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

    override fun existsByProductIdAndName(
        productId: Long,
        name: String,
    ): Boolean {
        return jpaProductOptionRepository.existsByProductProductIdAndName(productId, name)
    }
}
