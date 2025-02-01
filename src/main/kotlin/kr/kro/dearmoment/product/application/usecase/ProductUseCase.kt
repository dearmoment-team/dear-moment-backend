package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.product.application.dto.extensions.toDomain
import kr.kro.dearmoment.product.application.dto.extensions.toResponse
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.math.ceil

@Service
class ProductUseCase(
    private val productPersistencePort: ProductPersistencePort,
    private val productOptionPersistencePort: ProductOptionPersistencePort,
) {
    @Transactional
    fun saveProduct(request: CreateProductRequest): ProductResponse {
        val product = request.toDomain().copy(options = emptyList())
        validateForCreation(product)

        val savedProduct = productPersistencePort.save(product)
        saveProductOptions(savedProduct, request.options)

        val completeProduct =
            savedProduct.copy(
                options = productOptionPersistencePort.findByProductId(savedProduct.productId!!),
            )
        return completeProduct.toResponse()
    }

    @Transactional
    fun updateProduct(request: UpdateProductRequest): ProductResponse {
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
            options = productOptionPersistencePort.findByProductId(updatedProduct.productId!!),
        ).toResponse()
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
            totalPages = ((sortedProducts.size + size - 1) / size),
        )
    }

    /**
     * 메인페이지에 띄울 추천(모의 추천 수치 기반) 및 최근 일자 정렬 함수.
     * 첫 번째 정렬 기준은 모의 추천 수치(여기서는 인덱스+1로 가정), 두 번째 정렬 기준은 생성일(createdAt) 내림차순입니다.
     */
    @Transactional(readOnly = true)
    fun getMainPageProducts(
        page: Int = 0,
        size: Int = 10,
    ): PagedResponse<ProductResponse> {
        // 모든 상품을 조회합니다.
        val result =
            productPersistencePort.searchByCriteria(
                title = null,
                priceRange = null,
                typeCode = null,
                sortBy = null,
            )

        // 모의 추천 수치로 index+1을 사용 (실제 환경에서는 추천 점수를 조회)
        val mockData = result.mapIndexed { index, product -> Pair(product, index + 1) }

        // 첫 번째: 추천 수치 내림차순, 두 번째: 생성일(createdAt) 내림차순 정렬
        val sortedProducts =
            mockData.sortedWith(
                compareByDescending<Pair<Product, Int>> { it.second }
                    .thenByDescending { it.first.createdAt ?: LocalDateTime.MIN },
            ).map { it.first }

        // Pagination 처리
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

    private fun saveProductOptions(
        product: Product,
        options: List<CreateProductOptionRequest>,
    ): List<ProductOptionResponse> {
        return options.map { dto ->
            val domainOption = dto.toDomain(product.productId!!)
            productOptionPersistencePort.save(domainOption, product).toResponse()
        }
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
