package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ceil

@Service
class ProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
    private val productOptionPersistencePort: ProductOptionPersistencePort,
) : ProductUseCase {
    @Transactional
    override fun saveProduct(request: CreateProductRequest): ProductResponse {
        // 도메인 모델 생성 시 옵션은 빈 리스트로 초기화
        val product = CreateProductRequest.toDomain(request).copy(options = emptyList())
        validateForCreation(product)

        val savedProduct = productPersistencePort.save(product)
        saveProductOptions(savedProduct, request.options)

        val completeProduct =
            savedProduct.copy(
                options = productOptionPersistencePort.findByProductId(savedProduct.productId),
            )
        // 정적 팩토리 메서드를 이용하여 도메인 모델 → DTO 변환
        return ProductResponse.fromDomain(completeProduct)
    }

    @Transactional
    override fun updateProduct(request: UpdateProductRequest): ProductResponse {
        // 옵션 제외한 나머지 필드로 도메인 모델 생성, 옵션은 빈 리스트로 초기화
        val product = UpdateProductRequest.toDomain(request).copy(options = emptyList())
        product.validateForUpdate()

        val existingProduct =
            productPersistencePort.findById(product.productId)
                ?: throw IllegalArgumentException("Product not found: ${product.productId}")

        // 기존 옵션의 식별자는 모두 0L가 아닌 값으로 가정합니다.
        val existingOptionIds: Set<Long> = existingProduct.options.map { it.optionId }.toSet()

        // 업데이트 요청에 포함된 옵션 중, optionId가 0L가 아니라면 기존 옵션으로 판단
        val incomingOptionIds: Set<Long> =
            request.options
                .map { it.optionId ?: 0L }
                .filter { it != 0L }
                .toSet()

        // 기존 옵션 중 업데이트 요청에 없는 옵션은 삭제 대상
        val toDelete: Set<Long> = existingOptionIds subtract incomingOptionIds

        // 삭제 대상 옵션 처리
        handleDeletedOptions(toDelete)

        // 업데이트 요청에 포함된 각 옵션 처리
        request.options.forEach { dto ->
            val domainOption = UpdateProductOptionRequest.toDomain(dto, product.productId)
            if (domainOption.optionId != 0L) {
                val existingOptionEntity = productOptionPersistencePort.findById(domainOption.optionId)
                existingOptionEntity.name = domainOption.name
                existingOptionEntity.additionalPrice = domainOption.additionalPrice
                existingOptionEntity.description = domainOption.description
                productOptionPersistencePort.save(existingOptionEntity, product)
            } else {
                productOptionPersistencePort.save(domainOption, product)
            }
        }

        val updatedProduct = productPersistencePort.save(product)
        val completeProduct =
            updatedProduct.copy(
                options = productOptionPersistencePort.findByProductId(updatedProduct.productId),
            )
        return ProductResponse.fromDomain(completeProduct)
    }

    @Transactional
    override fun deleteProduct(productId: Long) {
        require(productPersistencePort.existsById(productId)) { "The product to delete does not exist: $productId." }
        productOptionPersistencePort.deleteAllByProductId(productId)
        productPersistencePort.deleteById(productId)
    }

    override fun getProductById(productId: Long): ProductResponse {
        val product =
            productPersistencePort.findById(productId)
                ?: throw IllegalArgumentException("Product with ID $productId not found.")

        val completeProduct =
            product.copy(
                options = productOptionPersistencePort.findByProductId(productId),
            )
        return ProductResponse.fromDomain(completeProduct)
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

        // 정렬 로직 (예시: 모의 추천 수치 사용)
        val mockData = result.mapIndexed { index, product -> Pair(product, index + 1) }
        val sortedProducts =
            when (sortBy) {
                "likes" -> mockData.sortedByDescending { it.second }.map { it.first }
                "price-asc" -> mockData.sortedBy { it.first.price }.map { it.first }
                "price-desc" -> mockData.sortedByDescending { it.first.price }.map { it.first }
                else -> mockData.map { it.first }
            }

        return PagedResponse(
            content = sortedProducts.map { ProductResponse.fromDomain(it) },
            page = page,
            size = size,
            totalElements = sortedProducts.size.toLong(),
            totalPages = ((sortedProducts.size + size - 1) / size),
        )
    }

    @Transactional(readOnly = true)
    override fun getMainPageProducts(
        page: Int,
        size: Int,
    ): PagedResponse<ProductResponse> {
        val result =
            productPersistencePort.searchByCriteria(
                title = null,
                priceRange = null,
                typeCode = null,
                sortBy = null,
            )

        val mockData = result.mapIndexed { index, product -> Pair(product, index + 1) }
        val sortedProducts =
            mockData.sortedWith(
                compareByDescending<Pair<Product, Int>> { it.second }
                    .thenByDescending { it.first.createdAt },
            ).map { it.first }

        val fromIndex = page * size
        val toIndex = if (fromIndex + size > sortedProducts.size) sortedProducts.size else fromIndex + size
        val pagedContent = if (fromIndex >= sortedProducts.size) emptyList() else sortedProducts.subList(fromIndex, toIndex)

        return PagedResponse(
            content = pagedContent.map { ProductResponse.fromDomain(it) },
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
        options: List<CreateProductOptionRequest>,
    ) {
        options.forEach { dto ->
            val domainOption = CreateProductOptionRequest.toDomain(dto, product.productId)
            productOptionPersistencePort.save(domainOption, product)
        }
    }

    private fun validatePriceRange(
        min: Long?,
        max: Long?,
    ) {
        require(!((min != null && min < 0) || (max != null && max < 0))) {
            "Price range must be greater than or equal to 0."
        }
        require(min == null || max == null || min <= max) {
            "Minimum price cannot exceed maximum price."
        }
    }

    private fun validateForCreation(product: Product) {
        require(!productPersistencePort.existsByUserIdAndTitle(product.userId, product.title)) {
            "A product with the same title already exists: ${product.title}."
        }
    }

    private fun handleDeletedOptions(toDelete: Set<Long>) {
        toDelete.forEach { optionId ->
            productOptionPersistencePort.deleteById(optionId)
        }
    }
}
