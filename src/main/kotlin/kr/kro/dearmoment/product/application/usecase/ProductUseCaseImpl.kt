package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.product.application.dto.extensions.toDomain
import kr.kro.dearmoment.product.application.dto.extensions.toResponse
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.math.ceil

@Service
class ProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
    private val productOptionPersistencePort: ProductOptionPersistencePort,
) : ProductUseCase {

    @Transactional
    override fun saveProduct(request: CreateProductRequest): ProductResponse {
        val product = request.toDomain().copy(options = emptyList())
        validateForCreation(product)

        val savedProduct = productPersistencePort.save(product)
        saveProductOptions(savedProduct, request.options)

        val completeProduct =
            savedProduct.copy(
                options = productOptionPersistencePort.findByProductId(savedProduct.productId!!)
            )
        return completeProduct.toResponse()
    }

    @Transactional
    override fun updateProduct(request: UpdateProductRequest): ProductResponse {
        val product = request.toDomain().copy(options = emptyList())
        product.validateForUpdate()

        val existingProduct =
            productPersistencePort.findById(product.productId!!)
                ?: throw IllegalArgumentException("Product not found: ${product.productId}")

        // 옵션 처리 (신규 생성, 업데이트, 삭제)
        val existingOptionIds = existingProduct.options.mapNotNull { it.optionId }.toSet()
        val incomingOptionIds = request.options.mapNotNull { it.optionId }.toSet()
        val toDelete = existingOptionIds subtract incomingOptionIds

        // 1. 삭제할 옵션 처리
        handleDeletedOptions(toDelete)

        request.options.forEach { dto ->
            val domainOption =
                dto.toDomain().copy(
                    productId = product.productId,
                )
            productOptionPersistencePort.save(domainOption, product)
        }

        val updatedProduct = productPersistencePort.save(product)
        return updatedProduct.copy(
            options = productOptionPersistencePort.findByProductId(updatedProduct.productId!!)
        ).toResponse()
    }

    @Transactional
    override fun deleteProduct(productId: Long) {
        if (!productPersistencePort.existsById(productId)) {
            throw IllegalArgumentException("The product to delete does not exist: $productId.")
        }
        productOptionPersistencePort.deleteAllByProductId(productId)
        productPersistencePort.deleteById(productId)
    }

    override fun getProductById(productId: Long): ProductResponse {
        val product =
            productPersistencePort.findById(productId)
                ?: throw IllegalArgumentException("Product with ID $productId not found.")

        val completeProduct =
            product.copy(
                options = productOptionPersistencePort.findByProductId(productId)
            )
        return completeProduct.toResponse()
    }

    override fun searchProducts(
        title: String?,
        minPrice: Long?,
        maxPrice: Long?,
        typeCode: Int?,
        sortBy: String?,
        page: Int,
        size: Int,
    ): PagedResponse<ProductResponse> {
        validatePriceRange(minPrice, maxPrice)

        val result =
            productPersistencePort.searchByCriteria(
                title = title,
                priceRange = minPrice?.let { Pair(it, maxPrice) },
                typeCode = typeCode,
                sortBy = sortBy,
            )

        // 정렬 로직 (예시로 모의 추천 수치를 사용)
        val mockData = result.mapIndexed { index, product -> Pair(product, index + 1) }
        val sortedProducts = when (sortBy) {
            "likes" -> mockData.sortedByDescending { it.second }.map { it.first }
            "price-asc" -> mockData.sortedBy { it.first.price }.map { it.first }
            "price-desc" -> mockData.sortedByDescending { it.first.price }.map { it.first }
            else -> mockData.map { it.first }
        }

        return PagedResponse(
            content = sortedProducts.map { it.toResponse() },
            page = page,
            size = size,
            totalElements = sortedProducts.size.toLong(),
            totalPages = ((sortedProducts.size + size - 1) / size)
        )
    }

    @Transactional(readOnly = true)
    override fun getMainPageProducts(page: Int, size: Int): PagedResponse<ProductResponse> {
        val result =
            productPersistencePort.searchByCriteria(
                title = null,
                priceRange = null,
                typeCode = null,
                sortBy = null,
            )

        val mockData = result.mapIndexed { index, product -> Pair(product, index + 1) }
        val sortedProducts = mockData.sortedWith(
            compareByDescending<Pair<Product, Int>> { it.second }
                .thenByDescending { it.first.createdAt ?: LocalDateTime.MIN }
        ).map { it.first }

        val fromIndex = page * size
        val toIndex = if (fromIndex + size > sortedProducts.size) sortedProducts.size else fromIndex + size
        val pagedContent = if (fromIndex >= sortedProducts.size) emptyList() else sortedProducts.subList(fromIndex, toIndex)

        return PagedResponse(
            content = pagedContent.map { it.toResponse() },
            page = page,
            size = size,
            totalElements = sortedProducts.size.toLong(),
            totalPages = ceil(sortedProducts.size.toDouble() / size).toInt(),
        )
    }

    // ──────────────────────────────────────────────
    // 헬퍼 메소드들
    private fun saveProductOptions(
        product: Product,
        options: List<CreateProductOptionRequest>
    ) {
        options.forEach { dto ->
            val domainOption = dto.toDomain(product.productId!!)
            productOptionPersistencePort.save(domainOption, product)
        }
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
