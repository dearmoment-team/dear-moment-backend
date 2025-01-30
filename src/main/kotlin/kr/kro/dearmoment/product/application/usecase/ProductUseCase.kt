package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.product.application.dto.extensions.toDomain
import kr.kro.dearmoment.product.application.dto.extensions.toResponse
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
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
    fun saveProduct(request: CreateProductRequest): ProductResponse {
        val product = request.toDomain()
        validateForCreation(product)

        val savedProduct = productPersistencePort.save(product)
        saveProductOptions(savedProduct, product.options.orEmpty()) // null 방지

        val completeProduct =
            savedProduct.copy(
                options = productOptionPersistencePort.findByProductId(savedProduct.productId!!),
            )
        return completeProduct.toResponse()
    }

    @Transactional
    fun updateProduct(request: UpdateProductRequest): ProductResponse {
        val product = request.toDomain()
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
        val completeProduct =
            updatedProduct.copy(
                options = productOptionPersistencePort.findByProductId(updatedProduct.productId!!),
            )
        return completeProduct.toResponse()
    }

    @Transactional
    fun deleteProduct(productId: Long) {
        if (!productPersistencePort.existsById(productId)) {
            throw IllegalArgumentException("The product to delete does not exist: $productId.")
        }
        productOptionPersistencePort.deleteAllByProductId(productId)
        productPersistencePort.deleteById(productId)
    }

    fun getProductById(productId: Long): ProductResponse {
        val product =
            productPersistencePort.findById(productId)
                ?: throw IllegalArgumentException("Product with ID $productId not found.")

        val completeProduct =
            product.copy(
                options = productOptionPersistencePort.findByProductId(productId),
            )
        return completeProduct.toResponse()
    }

    fun searchProducts(
        title: String?,
        minPrice: Long?,
        maxPrice: Long?,
        typeCode: Int? = null,
        sortBy: String? = null,
        page: Int = 0,
        size: Int = 10,
    ): PagedResponse<ProductResponse> {
        validatePriceRange(minPrice, maxPrice)

        val result =
            productPersistencePort.searchByCriteria(
                title = title,
                priceRange = minPrice?.let { Pair(it, maxPrice) },
                typeCode = typeCode,
                sortBy = sortBy,
            )

        val mockData =
            result.mapIndexed { index, product ->
                Pair(product, index + 1)
            }

        val sortedProducts =
            when (sortBy) {
                "likes" -> mockData.sortedByDescending { it.second }
                "price-asc" -> mockData.sortedBy { it.first.price }
                "price-desc" -> mockData.sortedByDescending { it.first.price }
                else -> mockData
            }.map { it.first }

        return PagedResponse(
            content = sortedProducts.map { it.toResponse() },
            page = page,
            size = size,
            totalElements = sortedProducts.size.toLong(),
            totalPages = (sortedProducts.size / size) + 1,
        )
    }

    private fun saveProductOptions(
        product: Product,
        options: List<ProductOption>,
    ): List<ProductOption> {
        return options.map { productOptionPersistencePort.save(it, product) }
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
