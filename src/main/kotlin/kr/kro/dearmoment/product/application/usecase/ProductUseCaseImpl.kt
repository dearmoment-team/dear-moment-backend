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
        // 도메인 모델 생성 시, 옵션은 빈 리스트로 초기화 (Auditing 정보는 엔티티에서만 관리하므로 도메인에는 null로 처리)
        val product = CreateProductRequest.toDomain(request).copy(options = emptyList())
        validateForCreation(product)
        val savedProduct = productPersistencePort.save(product)
        saveProductOptions(savedProduct, request.options)
        val completeProduct = savedProduct.copy(
            options = productOptionPersistencePort.findByProductId(savedProduct.productId)
        )
        return ProductResponse.fromDomain(completeProduct)
    }

    @Transactional
    override fun updateProduct(request: UpdateProductRequest): ProductResponse {
        // 옵션 제외한 나머지 필드로 도메인 모델 생성, 옵션은 빈 리스트로 초기화
        val product = UpdateProductRequest.toDomain(request).copy(options = emptyList())
        product.validateForUpdate()
        val existingProduct = productPersistencePort.findById(product.productId)
            ?: throw IllegalArgumentException("Product not found: ${product.productId}")

        // 기존 옵션과 업데이트 요청 옵션의 ID 비교
        val existingOptionIds: Set<Long> = existingProduct.options.map { it.optionId }.toSet()
        val incomingOptionIds: Set<Long> =
            request.options.map { it.optionId ?: 0L }
                .filter { it != 0L }
                .toSet()
        // 기존 옵션 중 업데이트 요청에 없는 옵션은 삭제 대상
        val toDelete: Set<Long> = existingOptionIds subtract incomingOptionIds
        handleDeletedOptions(toDelete)

        // 업데이트 요청에 포함된 각 옵션 저장 (도메인 변환 시 productId를 지정)
        request.options.forEach { dto ->
            val domainOption = UpdateProductOptionRequest.toDomain(dto, product.productId)
            productOptionPersistencePort.save(domainOption, product)
        }

        val updatedProduct = productPersistencePort.save(product)
        val completeProduct = updatedProduct.copy(
            options = productOptionPersistencePort.findByProductId(updatedProduct.productId)
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
        val product = productPersistencePort.findById(productId)
            ?: throw IllegalArgumentException("Product with ID $productId not found.")
        val completeProduct = product.copy(
            options = productOptionPersistencePort.findByProductId(productId)
        )
        return ProductResponse.fromDomain(completeProduct)
    }

    override fun searchProducts(
        title: String?,
        productType: String?,
        shootingPlace: String?,
        sortBy: String?,
        page: Int,
        size: Int,
    ): PagedResponse<ProductResponse> {
        val result = productPersistencePort.searchByCriteria(
            title = title,
            productType = productType,
            shootingPlace = shootingPlace,
            sortBy = sortBy
        )
        // 단순하게 index 기준으로 정렬하여 페이징 처리
        val sortedProducts = result
        val fromIndex = page * size
        val toIndex = if (fromIndex + size > sortedProducts.size) sortedProducts.size else fromIndex + size
        val pagedContent =
            if (fromIndex >= sortedProducts.size) emptyList() else sortedProducts.subList(fromIndex, toIndex)
        return PagedResponse(
            content = pagedContent.map { ProductResponse.fromDomain(it) },
            page = page,
            size = size,
            totalElements = sortedProducts.size.toLong(),
            totalPages = ceil(sortedProducts.size.toDouble() / size).toInt()
        )
    }

    @Transactional(readOnly = true)
    override fun getMainPageProducts(page: Int, size: Int): PagedResponse<ProductResponse> {
        val result = productPersistencePort.searchByCriteria(
            title = null,
            productType = null,
            shootingPlace = null,
            sortBy = null
        )
        val sortedProducts = result
        val fromIndex = page * size
        val toIndex = if (fromIndex + size > sortedProducts.size) sortedProducts.size else fromIndex + size
        val pagedContent =
            if (fromIndex >= sortedProducts.size) emptyList() else sortedProducts.subList(fromIndex, toIndex)
        return PagedResponse(
            content = pagedContent.map { ProductResponse.fromDomain(it) },
            page = page,
            size = size,
            totalElements = sortedProducts.size.toLong(),
            totalPages = ceil(sortedProducts.size.toDouble() / size).toInt()
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
