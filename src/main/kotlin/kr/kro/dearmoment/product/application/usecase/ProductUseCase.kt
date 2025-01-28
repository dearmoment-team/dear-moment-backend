package kr.kro.dearmoment.product.application.usecase

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption

@Service
class ProductUseCase(
    private val productPersistencePort: ProductPersistencePort,
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val productEntityRetrievalPort: ProductEntityRetrievalPort,
) {

    @Transactional
    fun saveProduct(product: Product): Product {
        validateForCreation(product)
        val savedProduct = productPersistencePort.save(product)
        saveProductOptions(savedProduct.productId!!, product.options)
        return savedProduct.copy(options = productOptionPersistencePort.findByProduct(savedProduct))
    }

    @Transactional
    fun updateProduct(product: Product): Product {
        product.validateForUpdate()

        val existingProduct = productEntityRetrievalPort.getProductById(product.productId!!)
            ?: throw IllegalArgumentException("Product with ID ${product.productId} not found.")

        val (updatedOptions, toDelete) = existingProduct.updateOptions(product.options)

        handleDeletedOptions(toDelete)

        val updatedProduct = productPersistencePort.save(product)

        updatedOptions.forEach { productOptionPersistencePort.save(it) }

        return updatedProduct.copy(options = productOptionPersistencePort.findByProduct(updatedProduct))
    }

    @Transactional
    fun deleteProduct(productId: Long) {
        if (!productEntityRetrievalPort.existsById(productId)) {
            throw IllegalArgumentException("The product to delete does not exist: $productId.")
        }
        productOptionPersistencePort.deleteAllByProductId(productId)
        productPersistencePort.deleteById(productId)
    }

    fun getProductById(productId: Long): Product {
        val product = productEntityRetrievalPort.getProductById(productId)
            ?: throw IllegalArgumentException("Product with ID $productId not found.")
        return product.copy(options = productOptionPersistencePort.findByProduct(product))
    }

    private fun saveProductOptions(productId: Long, options: List<ProductOption>): List<ProductOption> {
        return options.map { productOptionPersistencePort.save(it.copy(productId = productId)) }
    }

    fun searchProducts(title: String?, minPrice: Long?, maxPrice: Long?): List<Product> {
        validatePriceRange(minPrice, maxPrice)
        return productPersistencePort.searchByCriteria(
            title = title,
            priceRange = minPrice?.let { Pair(it, maxPrice) }
        )
    }

    private fun validatePriceRange(min: Long?, max: Long?) {
        if ((min != null && min < 0) || (max != null && max < 0)) {
            throw IllegalArgumentException("Price range must be greater than or equal to 0.")
        }
        if (min != null && max != null && min > max) {
            throw IllegalArgumentException("Minimum price cannot exceed maximum price.")
        }
    }

    private fun validateForCreation(product: Product) {
        if (productPersistencePort.existsByUserIdAndTitle(product.userId!!, product.title)) {
            throw IllegalArgumentException("A product with the same title already exists: ${product.title}.")
        }
    }

    private fun handleDeletedOptions(toDelete: Set<Long>) {
        toDelete.forEach { optionId ->
            productOptionPersistencePort.deleteById(optionId)
        }
    }
}
