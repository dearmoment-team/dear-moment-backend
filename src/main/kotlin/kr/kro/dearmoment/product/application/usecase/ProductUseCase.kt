package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.product.application.dto.PagedResponse
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductUseCase(
    private val productPersistencePort: ProductPersistencePort,
    private val productOptionPersistencePort: ProductOptionPersistencePort,
) {
    @Transactional
    fun saveProduct(product: Product): Product {
        validateForCreation(product)
        val savedProduct = productPersistencePort.save(product)
        saveProductOptions(savedProduct, product.options)
        return savedProduct.copy(options = productOptionPersistencePort.findByProductId(savedProduct.productId!!))
    }

    @Transactional
    fun updateProduct(product: Product): Product {
        product.validateForUpdate()

        val existingProduct =
            productPersistencePort.findById(product.productId!!)
                ?: throw IllegalArgumentException("Product with ID ${product.productId} not found.")

        val existingOptionIds = existingProduct.options.mapNotNull { it.optionId }.toSet()
        val incomingOptionIds = product.options.mapNotNull { it.optionId }.toSet()
        val toDelete = existingOptionIds subtract incomingOptionIds

        handleDeletedOptions(toDelete)

        product.options.forEach { option ->
            productOptionPersistencePort.save(option, product)
        }

        val updatedProduct = productPersistencePort.save(product)
        return updatedProduct.copy(options = productOptionPersistencePort.findByProductId(updatedProduct.productId!!))
    }

    @Transactional
    fun deleteProduct(productId: Long) {
        if (!productPersistencePort.existsById(productId)) {
            throw IllegalArgumentException("The product to delete does not exist: $productId.")
        }
        productOptionPersistencePort.deleteAllByProductId(productId)
        productPersistencePort.deleteById(productId)
    }

    fun getProductById(productId: Long): Product {
        val product =
            productPersistencePort.findById(productId)
                ?: throw IllegalArgumentException("Product with ID $productId not found.")
        return product.copy(options = productOptionPersistencePort.findByProductId(productId))
    }

    private fun saveProductOptions(
        product: Product,
        options: List<ProductOption>,
    ): List<ProductOption> {
        return options.map { productOptionPersistencePort.save(it, product) }
    }

    fun searchProducts(
        title: String?,
        minPrice: Long?,
        maxPrice: Long?,
        typeCode: Int? = null,
        sortBy: String? = null,
        page: Int = 0,
        size: Int = 10,
    ): PagedResponse<Product> {
        validatePriceRange(minPrice, maxPrice)

        val result =
            productPersistencePort.searchByCriteria(
                title = title,
                priceRange = minPrice?.let { Pair(it, maxPrice) },
                typeCode = typeCode,
                sortBy = sortBy,
            )

        val mockData =
            result.map { product ->
                Pair(product, (1..100).random())
            }

        val sortedProducts =
            when (sortBy) {
                "likes" -> mockData.sortedByDescending { it.second }
                "price-asc" -> mockData.sortedBy { it.first.price }
                "price-desc" -> mockData.sortedByDescending { it.first.price }
                else -> mockData
            }.map { it.first }

        return PagedResponse(
            content = sortedProducts,
            page = page,
            size = size,
            totalElements = sortedProducts.size.toLong(),
            totalPages = (sortedProducts.size / size) + 1,
        )
    }

    private fun validatePriceRange(
        min: Long?,
        max: Long?,
    ) {
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
