package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductOptionUseCaseImpl(
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val productPersistencePort: ProductPersistencePort,
) : ProductOptionUseCase {
    @Transactional
    override fun saveProductOption(
        productId: Long,
        request: CreateProductOptionRequest,
    ): ProductOptionResponse {
        val product = getProductOrThrow(productId)
        validateDuplicateOption(productId, request.name)

        val option = CreateProductOptionRequest.toDomain(request, productId)
        val savedOption = productOptionPersistencePort.save(option, product)
        return ProductOptionResponse.fromDomain(savedOption)
    }

    @Transactional(readOnly = true)
    override fun getProductOptionById(optionId: Long): ProductOptionResponse {
        return ProductOptionResponse.fromDomain(productOptionPersistencePort.findById(optionId))
    }

    @Transactional(readOnly = true)
    override fun getAllProductOptions(): List<ProductOptionResponse> {
        return productOptionPersistencePort.findAll().map { ProductOptionResponse.fromDomain(it) }
    }

    @Transactional
    override fun deleteProductOptionById(optionId: Long) {
        productOptionPersistencePort.deleteById(optionId)
    }

    @Transactional(readOnly = true)
    override fun getProductOptionsByProductId(productId: Long): List<ProductOptionResponse> {
        return productOptionPersistencePort.findByProductId(productId).map { ProductOptionResponse.fromDomain(it) }
    }

    @Transactional
    override fun deleteAllProductOptionsByProductId(productId: Long) {
        productOptionPersistencePort.deleteAllByProductId(productId)
    }

    @Transactional(readOnly = true)
    override fun existsProductOptions(productId: Long): Boolean {
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
