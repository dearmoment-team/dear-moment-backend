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
import kotlin.math.min

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

        // 저장된 제품에 대해 옵션을 조회하여 완전한 도메인 모델 생성
        val completeProduct = enrichProduct(savedProduct)
        return ProductResponse.fromDomain(completeProduct)
    }

    @Transactional
    override fun updateProduct(request: UpdateProductRequest): ProductResponse {
        // 옵션 제외한 나머지 필드로 도메인 모델 생성, 옵션은 빈 리스트로 초기화
        val product = UpdateProductRequest.toDomain(request).copy(options = emptyList())
        product.validateForUpdate()

        val existingProduct = productPersistencePort.findById(product.productId)
            ?: throw IllegalArgumentException("Product not found: ${product.productId}")

        // 기존 옵션의 식별자 집합과 업데이트 요청에 포함된 옵션 식별자 집합 계산
        val existingOptionIds: Set<Long> = existingProduct.options.map { it.optionId }.toSet()
        val incomingOptionIds: Set<Long> = request.options.mapNotNull { it.optionId }
            .filter { it != 0L }
            .toSet()

        // 기존 옵션 중 업데이트 요청에 없는 옵션은 삭제 대상
        val toDelete = existingOptionIds subtract incomingOptionIds
        handleDeletedOptions(toDelete)

        // 요청에 포함된 각 옵션에 대해 업데이트 또는 신규 등록 처리
        request.options.forEach { dto ->
            processProductOption(dto, product)
        }

        val updatedProduct = productPersistencePort.save(product)
        val completeProduct = enrichProduct(updatedProduct)
        return ProductResponse.fromDomain(completeProduct)
    }

    @Transactional
    override fun deleteProduct(productId: Long) {
        require(productPersistencePort.existsById(productId)) {
            "The product to delete does not exist: $productId."
        }
        productOptionPersistencePort.deleteAllByProductId(productId)
        productPersistencePort.deleteById(productId)
    }

    override fun getProductById(productId: Long): ProductResponse {
        val product = productPersistencePort.findById(productId)
            ?: throw IllegalArgumentException("Product with ID $productId not found.")
        val completeProduct = enrichProduct(product)
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

        val result = productPersistencePort.searchByCriteria(
            title = title,
            priceRange = minPrice?.let { Pair(it, maxPrice) },
            typeCode = typeCode,
            sortBy = sortBy,
        )

        // 예시로 index를 활용한 모의 정렬 데이터 생성
        val mockData = result.mapIndexed { index, product -> Pair(product, index + 1) }
        val sortedProducts = when (sortBy) {
            "likes" -> mockData.sortedByDescending { it.second }.map { it.first }
            "price-asc" -> mockData.sortedBy { it.first.price }.map { it.first }
            "price-desc" -> mockData.sortedByDescending { it.first.price }.map { it.first }
            else -> mockData.map { it.first }
        }

        // 페이징 처리
        val totalElements = sortedProducts.size.toLong()
        val totalPages = if (size > 0) ((sortedProducts.size + size - 1) / size) else 0
        val fromIndex = page * size
        val toIndex = min(fromIndex + size, sortedProducts.size)
        val pagedContent = if (fromIndex >= sortedProducts.size) emptyList() else sortedProducts.subList(fromIndex, toIndex)

        return PagedResponse(
            content = pagedContent.map { ProductResponse.fromDomain(it) },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages
        )
    }

    @Transactional(readOnly = true)
    override fun getMainPageProducts(
        page: Int,
        size: Int,
    ): PagedResponse<ProductResponse> {
        val result = productPersistencePort.searchByCriteria(
            title = null,
            priceRange = null,
            typeCode = null,
            sortBy = null,
        )

        val mockData = result.mapIndexed { index, product -> Pair(product, index + 1) }
        val sortedProducts = mockData.sortedWith(
            compareByDescending<Pair<Product, Int>> { it.second }
                .thenByDescending { it.first.createdAt },
        ).map { it.first }

        val totalElements = sortedProducts.size.toLong()
        val totalPages = ceil(sortedProducts.size.toDouble() / size).toInt()
        val fromIndex = page * size
        val toIndex = min(fromIndex + size, sortedProducts.size)
        val pagedContent = if (fromIndex >= sortedProducts.size) emptyList() else sortedProducts.subList(fromIndex, toIndex)

        return PagedResponse(
            content = pagedContent.map { ProductResponse.fromDomain(it) },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }

    // ──────────────────────────────────────────────
    // 헬퍼 메소드들

    /**
     * 제품 도메인 모델에 대해 옵션 리스트를 채워 완전한 모델로 반환합니다.
     */
    private fun enrichProduct(product: Product): Product {
        return product.copy(options = productOptionPersistencePort.findByProductId(product.productId))
    }

    /**
     * 업데이트 요청에 포함된 옵션 DTO를 기반으로 옵션을 업데이트하거나 신규 등록합니다.
     */
    private fun processProductOption(dto: UpdateProductOptionRequest, product: Product) {
        val domainOption = UpdateProductOptionRequest.toDomain(dto, product.productId)
        if (domainOption.optionId != 0L) {
            val existingOptionEntity = productOptionPersistencePort.findById(domainOption.optionId)
            existingOptionEntity.apply {
                name = domainOption.name
                additionalPrice = domainOption.additionalPrice
                description = domainOption.description
            }
            productOptionPersistencePort.save(existingOptionEntity, product)
        } else {
            productOptionPersistencePort.save(domainOption, product)
        }
    }

    /**
     * 생성 요청에 포함된 옵션들을 제품에 등록합니다.
     */
    private fun saveProductOptions(
        product: Product,
        options: List<CreateProductOptionRequest>,
    ) {
        options.forEach { dto ->
            val domainOption = CreateProductOptionRequest.toDomain(dto, product.productId)
            productOptionPersistencePort.save(domainOption, product)
        }
    }

    /**
     * 가격 범위가 올바른지 검증합니다.
     */
    private fun validatePriceRange(min: Long?, max: Long?) {
        require(!(min != null && min < 0 || max != null && max < 0)) {
            "Price range must be greater than or equal to 0."
        }
        require(min == null || max == null || min <= max) {
            "Minimum price cannot exceed maximum price."
        }
    }

    /**
     * 제품 생성 시 동일 사용자와 동일 제목의 제품이 존재하는지 검증합니다.
     */
    private fun validateForCreation(product: Product) {
        require(!productPersistencePort.existsByUserIdAndTitle(product.userId, product.title)) {
            "A product with the same title already exists: ${product.title}."
        }
    }

    /**
     * 삭제 대상 옵션들을 삭제합니다.
     */
    private fun handleDeletedOptions(toDelete: Set<Long>) {
        toDelete.forEach { optionId ->
            productOptionPersistencePort.deleteById(optionId)
        }
    }
}
