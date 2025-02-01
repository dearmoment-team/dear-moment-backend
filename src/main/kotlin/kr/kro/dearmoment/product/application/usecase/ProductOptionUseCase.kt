package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.product.application.dto.extensions.toDomain
import kr.kro.dearmoment.product.application.dto.extensions.toResponse
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductOptionUseCase(
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val productPersistencePort: ProductPersistencePort,
) {
    @Transactional
    fun saveProductOption(
        productId: Long,
        request: CreateProductOptionRequest,
    ): ProductOptionResponse {
        val product = getProductOrThrow(productId)
        validateDuplicateOption(productId, request.name)

        val option = request.toDomain(productId)
        val savedOption = productOptionPersistencePort.save(option, product)
        return savedOption.toResponse()
    }

    @Transactional(readOnly = true)
    fun getProductOptionById(optionId: Long): ProductOptionResponse {
        return productOptionPersistencePort.findById(optionId)
            .toResponse()
    }

    @Transactional(readOnly = true)
    fun getAllProductOptions(): List<ProductOptionResponse> {
        return productOptionPersistencePort.findAll()
            .map { it.toResponse() }
    }

    @Transactional
    fun deleteProductOptionById(optionId: Long) {
        productOptionPersistencePort.deleteById(optionId)
    }

    @Transactional(readOnly = true)
    fun getProductOptionsByProductId(productId: Long): List<ProductOptionResponse> {
        return productOptionPersistencePort.findByProductId(productId)
            .map { it.toResponse() }
    }

    @Transactional
    fun deleteAllProductOptionsByProductId(productId: Long) {
        productOptionPersistencePort.deleteAllByProductId(productId)
    }

    @Transactional(readOnly = true)
    fun existsProductOptions(productId: Long): Boolean {
        return productOptionPersistencePort.existsByProductId(productId)
    }

    private fun getProductOrThrow(productId: Long): Product {
        return productPersistencePort.findById(productId)
            ?: throw IllegalArgumentException("Product not found: $productId")
    }

    private fun validateDuplicateOption(
        productId: Long,
        name: String,
    ) {
        val exists = productOptionPersistencePort.existsByProductIdAndName(productId, name)
        if (exists) throw IllegalArgumentException("Duplicate option name: $name")
    }
}
