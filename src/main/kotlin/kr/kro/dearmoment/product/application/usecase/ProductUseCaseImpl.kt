package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.adapter.out.persistence.PartnerShopEmbeddable
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.service.ProductImageService
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import kotlin.math.ceil
import kotlin.math.min

@Service
class ProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val productImageService: ProductImageService,
) : ProductUseCase {
    // 제품 생성 유스케이스
    // 1. 전달받은 MultipartFile 목록을 ProductImageService.uploadImages()를 이용해 업로드한 후 Image 도메인 객체 리스트를 생성
    // 2. 생성된 Image 리스트를 포함해 CreateProductRequest를 통해 제품 도메인 모델을 구성
    // 3. 제품 도메인을 저장하고 옵션을 별도 저장한 후 최종 제품 정보를 응답 DTO로 반환한다.
    @Transactional
    override fun saveProduct(
        request: CreateProductRequest,
        images: List<MultipartFile>,
    ): ProductResponse {
        val uploadedImages: List<Image> = productImageService.uploadImages(images, request.userId)
        val product: Product =
            CreateProductRequest.toDomain(request, uploadedImages)
                .copy(options = emptyList())
        validateForCreation(product)
        val savedProduct: Product = productPersistencePort.save(product)
        saveProductOptions(savedProduct, request.options)
        val completeProduct: Product = enrichProduct(savedProduct)
        return ProductResponse.fromDomain(completeProduct)
    }

    // 제품 업데이트 유스케이스
    // 1. 신규 MultipartFile 목록이 있다면, ProductImageService.uploadNewImagesWithPlaceholders()를 통해 업로드 및 플레이스홀더 매핑 수행
    // 2. 프론트엔드에서 전달된 이미지 식별자 목록을 ProductImageService.resolveFinalImageOrder()로 최종 Image 도메인 객체 리스트로 구성
    // 3. UpdateProductRequest를 통해 제품 도메인 모델을 생성 및 검증하고, 기존 제품을 조회하여 엔티티로 변환
    // 4. 기존 엔티티의 이미지 컬렉션을 ProductImageService.synchronizeProductImages()를 통해 최종 이미지 목록과 동기화한 후,
    //    나머지 필드와 옵션을 업데이트하고 저장하여 응답 DTO로 반환한다.
    @Transactional
    override fun updateProduct(
        request: UpdateProductRequest,
        images: List<MultipartFile>?,
    ): ProductResponse {
        val newImageMappings: Map<String, Image> = productImageService.uploadNewImagesWithPlaceholders(images, request.userId)
        val requestedImageIdentifiers: List<String> = request.images.map { it.identifier }
        val finalImageOrder: List<Image> =
            productImageService.resolveFinalImageOrder(
                requestedImageIdentifiers,
                newImageMappings,
                request.userId,
            )
        val productFromRequest: Product =
            UpdateProductRequest.toDomain(request, finalImageOrder)
                .copy(options = emptyList())
        productFromRequest.validateForUpdate()
        val existingProductDomain: Product =
            productPersistencePort.findById(productFromRequest.productId)
                ?: throw IllegalArgumentException("Product not found: ${productFromRequest.productId}")
        val existingProductEntity: ProductEntity = ProductEntity.fromDomain(existingProductDomain)
        productImageService.synchronizeProductImages(existingProductEntity, finalImageOrder, newImageMappings, request.userId)
        updateProductEntity(existingProductEntity, request)
        val existingOptions = productOptionPersistencePort.findByProductId(productFromRequest.productId)
        val updateOptionIds =
            request.options
                .filter { it.optionId != null && it.optionId != 0L }
                .map { it.optionId!! }
                .toSet()
        existingOptions.filter { it.optionId !in updateOptionIds }
            .forEach { productOptionPersistencePort.deleteById(it.optionId) }
        request.options.forEach { dto ->
            processProductOption(dto, productFromRequest)
        }
        val updatedDomain: Product = productPersistencePort.save(existingProductEntity.toDomain())
        val completeProduct: Product = enrichProduct(updatedDomain)
        return ProductResponse.fromDomain(completeProduct)
    }

    // 제품 삭제 유스케이스: 제품 존재 여부를 확인한 후, 관련 옵션과 엔티티를 삭제한다.
    @Transactional
    override fun deleteProduct(productId: Long) {
        require(productPersistencePort.existsById(productId)) {
            "The product to delete does not exist: $productId."
        }
        productOptionPersistencePort.deleteAllByProductId(productId)
        productPersistencePort.deleteById(productId)
    }

    // 제품 ID로 조회 후, 응답 DTO(ProductResponse)로 반환한다.
    override fun getProductById(productId: Long): ProductResponse {
        val product: Product =
            productPersistencePort.findById(productId)
                ?: throw IllegalArgumentException("Product with ID $productId not found.")
        val completeProduct: Product = enrichProduct(product)
        return ProductResponse.fromDomain(completeProduct)
    }

    // 조건에 따른 제품 검색 후, 페이지네이션된 응답 DTO를 반환한다.
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
        val result: List<Product> =
            productPersistencePort.searchByCriteria(
                title = title,
                priceRange = minPrice?.let { Pair(it, maxPrice) },
                typeCode = typeCode,
                sortBy = sortBy,
            )
        val mockData = result.mapIndexed { index, product -> Pair(product, index + 1) }
        val sortedProducts =
            when (sortBy) {
                "likes" -> mockData.sortedByDescending { it.second }.map { it.first }
                "price-asc" -> mockData.sortedBy { it.first.price }.map { it.first }
                "price-desc" -> mockData.sortedByDescending { it.first.price }.map { it.first }
                else -> mockData.map { it.first }
            }
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
            totalPages = totalPages,
        )
    }

    // 메인 페이지 제품 목록 검색 후, 페이지네이션된 응답 DTO를 반환한다.
    override fun getMainPageProducts(
        page: Int,
        size: Int,
    ): PagedResponse<ProductResponse> {
        val result: List<Product> =
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
    // 제품 도메인 모델 업데이트, 옵션 처리 및 검증 관련 헬퍼 메소드들

    private fun updateProductEntity(
        entity: ProductEntity,
        domain: UpdateProductRequest,
    ) {
        with(entity) {
            title = domain.title
            description = domain.description
            price = domain.price
            typeCode = domain.typeCode
            concept = domain.concept
            originalProvideType = domain.originalProvideType
            partialOriginalCount = domain.partialOriginalCount
            shootingTime = domain.shootingTime
            shootingLocation = domain.shootingLocation
            numberOfCostumes = domain.numberOfCostumes
            seasonYear = domain.seasonYear
            seasonHalf = domain.seasonHalf
            partnerShops = domain.partnerShops.map { ps -> PartnerShopEmbeddable(ps.name, ps.link) }
            detailedInfo = domain.detailedInfo
            warrantyInfo = domain.warrantyInfo
            contactInfo = domain.contactInfo
        }
    }

    private fun processProductOption(
        dto: UpdateProductOptionRequest,
        product: Product,
    ) {
        val domainOption = UpdateProductOptionRequest.toDomain(dto, product.productId)
        if (domainOption.optionId != 0L) {
            val existingOption = productOptionPersistencePort.findById(domainOption.optionId)
            existingOption.let {
                it.name = domainOption.name
                it.additionalPrice = domainOption.additionalPrice
                it.description = domainOption.description
                productOptionPersistencePort.save(it, product)
            }
        } else {
            productOptionPersistencePort.save(domainOption, product)
        }
    }

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
        require(!(min != null && min < 0 || max != null && max < 0)) {
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

    private fun enrichProduct(product: Product): Product {
        return product.copy(options = productOptionPersistencePort.findByProductId(product.productId))
    }
}
