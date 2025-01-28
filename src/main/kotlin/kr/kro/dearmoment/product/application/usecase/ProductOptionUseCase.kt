package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductOptionUseCase(
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val productPersistencePort: ProductPersistencePort,
) {
    @Transactional
    fun saveProductOption(productOption: ProductOption): ProductOption {
        val productId =
            productOption.productId
                ?: throw IllegalArgumentException("Product ID must be provided")

        val product =
            productPersistencePort.findById(productId)
                ?: throw IllegalArgumentException("Product with ID $productId not found")

        val existingOptions = productOptionPersistencePort.findByProductId(productId)
        if (existingOptions.any { it.name == productOption.name }) {
            throw IllegalArgumentException("Duplicate option name: ${productOption.name}")
        }

        return productOptionPersistencePort.save(productOption, product)
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
        return productOptionPersistencePort.findByProductId(productId)
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
