package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.stereotype.Repository

@Repository
class ProductOptionRepositoryAdapter(
    private val jpaProductOptionRepository: JpaProductOptionRepository,
    private val jpaProductRepository: JpaProductRepository,
) : ProductOptionPersistencePort {
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

    override fun deleteById(id: Long) {
        if (!jpaProductOptionRepository.existsById(id)) {
            throw CustomException(ErrorCode.OPTION_NOT_FOUND)
        }
        jpaProductOptionRepository.deleteById(id)
    }

    override fun deleteAllByProductId(productId: Long) {
        jpaProductOptionRepository.deleteAllByProductProductId(productId)
    }
}
