package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductOptionUseCase(
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val productEntityRetrievalPort: ProductEntityRetrievalPort
) {

    @Transactional
    fun saveProductOption(productOption: ProductOption): ProductOption {
        val productId = productOption.productId
            ?: throw IllegalArgumentException("Product ID must be provided")

        val productEntity = productEntityRetrievalPort.getProductById(productId)?.let { ProductEntity.fromDomain(it) }
            ?: throw IllegalArgumentException("Product with ID $productId not found")

        if (productOptionPersistencePort.existsByProductId(productId)) {
            throw IllegalArgumentException("Duplicate option name: ${productOption.name}")
        }

        return productOptionPersistencePort.save(productOption, productEntity)
    }

    @Transactional(readOnly = true)
    fun getProductOptionById(optionId: Long): ProductOption {
        return productOptionPersistencePort.findById(optionId)
    }

    @Transactional(readOnly = true)
    fun getAllProductOptions(): List<ProductOption> {
        return productOptionPersistencePort.findAll()
    }

    @Transactional
    fun deleteProductOptionById(optionId: Long) {
        productOptionPersistencePort.deleteById(optionId)
    }

    @Transactional(readOnly = true)
    fun getProductOptionsByProductId(productId: Long): List<ProductOption> {
        val product = productEntityRetrievalPort.getProductById(productId)
            ?: throw IllegalArgumentException("Product with ID $productId not found")

        return productOptionPersistencePort.findByProduct(product)
    }

    @Transactional
    fun deleteAllProductOptionsByProductId(productId: Long) {
        productOptionPersistencePort.deleteAllByProductId(productId)
    }

    @Transactional(readOnly = true)
    fun existsProductOptions(productId: Long): Boolean {
        return productOptionPersistencePort.existsByProductId(productId)
    }
}
