package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.image.adapter.output.persistence.ImageEntity
import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.application.service.ImageService
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
    private val imageService: ImageService,
) : ProductUseCase {
    @Transactional
    override fun saveProduct(
        request: CreateProductRequest,
        images: List<MultipartFile>,
    ): ProductResponse {
        // 1. 새 이미지 업로드: 각 파일을 처리하여 파일명 목록을 수집
        val uploadedImages: List<String> = uploadImages(images, request.userId)
        // 2. 도메인 모델 생성 시 업로드된 이미지 목록을 주입 (옵션은 빈 리스트로 초기화)
        val product: Product = CreateProductRequest.toDomain(request, uploadedImages).copy(options = emptyList())
        validateForCreation(product)
        // 3. 저장 (영속성 어댑터 내부에서 도메인과 엔티티 간 변환 수행)
        val savedProduct: Product = productPersistencePort.save(product)
        // 4. 제품 옵션 저장
        saveProductOptions(savedProduct, request.options)
        // 5. 제품 정보 완성 후 응답 생성
        val completeProduct: Product = enrichProduct(savedProduct)
        return ProductResponse.fromDomain(completeProduct)
    }

    @Transactional
    override fun updateProduct(
        request: UpdateProductRequest,
        images: List<MultipartFile>?,
    ): ProductResponse {
        // 1. 신규 이미지 업로드 및 파일명 매핑
        val newImageMappings: Map<String, String> = uploadNewImagesWithPlaceholders(images, request.userId)
        val finalImageOrder: List<String> = resolveFinalImageOrder(request.images, newImageMappings)

        // 2. 도메인 객체 생성 및 유효성 검증 (최종 이미지 순서를 사용)
        val productFromRequest: Product = UpdateProductRequest.toDomain(request, finalImageOrder).copy(options = emptyList())
        productFromRequest.validateForUpdate()

        // 3. 기존 제품 조회 (도메인 Product 반환)
        val existingProductDomain: Product =
            productPersistencePort.findById(productFromRequest.productId)
                ?: throw IllegalArgumentException("Product not found: ${productFromRequest.productId}")
        // 4. 도메인 Product를 관리 엔티티(ProductEntity)로 변환
        val existingProductEntity: ProductEntity = ProductEntity.fromDomain(existingProductDomain)

        // 5. 이미지 동기화 작업 (엔티티의 images 컬렉션 업데이트)
        synchronizeProductImages(
            existingProduct = existingProductEntity,
            finalImageNames = finalImageOrder,
            newImageMappings = newImageMappings,
            userId = request.userId,
        )

        // 6. 엔티티의 나머지 필드를 업데이트 (헬퍼 함수)
        updateProductEntity(existingProductEntity, request)

        // 6.5. 옵션 업데이트 처리
        // 기존 옵션 목록 조회
        val existingOptions = productOptionPersistencePort.findByProductId(productFromRequest.productId)
        // 요청에 포함된 옵션 중, optionId가 0L 또는 null인 것은 신규 옵션이고, 나머지는 업데이트 대상임
        val updateOptionIds =
            request.options
                .filter { it.optionId != null && it.optionId != 0L }
                .map { it.optionId!! }
                .toSet()
        // 기존 옵션 중 요청에 없는 옵션은 삭제 처리
        existingOptions.filter { it.optionId !in updateOptionIds }
            .forEach { productOptionPersistencePort.deleteById(it.optionId) }
        // 요청에 포함된 각 옵션을 처리 (업데이트 또는 신규 생성)
        request.options.forEach { dto ->
            processProductOption(dto, productFromRequest)
        }

        // 7. 엔티티를 도메인으로 변환하여 저장 및 응답 생성
        val updatedDomain: Product = productPersistencePort.save(existingProductEntity.toDomain())
        val completeProduct: Product = enrichProduct(updatedDomain)
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
        val product: Product =
            productPersistencePort.findById(productId)
                ?: throw IllegalArgumentException("Product with ID $productId not found.")
        val completeProduct: Product = enrichProduct(product)
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
    // 헬퍼 메소드들

    private fun enrichProduct(product: Product): Product {
        return product.copy(options = productOptionPersistencePort.findByProductId(product.productId))
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

    override fun uploadImages(
        images: List<MultipartFile>,
        userId: Long,
    ): List<String> {
        return images.map { file ->
            val command = SaveImageCommand(file, userId)
            val imageId = imageService.save(command)
            imageService.getOne(imageId).fileName
        }
    }

    private fun uploadNewImagesWithPlaceholders(
        newImages: List<MultipartFile>?,
        userId: Long,
    ): Map<String, String> {
        if (newImages.isNullOrEmpty()) return emptyMap()
        val uploaded = uploadImages(newImages, userId)
        return newImages.mapIndexed { idx, _ -> "new_$idx" to uploaded[idx] }.toMap()
    }

    private fun resolveFinalImageOrder(
        requestedImages: List<String>,
        newImageMappings: Map<String, String>,
    ): List<String> {
        return requestedImages.map { name -> newImageMappings[name] ?: name }
    }

    private fun synchronizeProductImages(
        existingProduct: ProductEntity,
        finalImageNames: List<String>,
        newImageMappings: Map<String, String>,
        userId: Long,
    ) {
        val existingImages = existingProduct.images.associateBy { it.fileName }
        val desiredSet = finalImageNames.toSet()

        // 기존 이미지 중 삭제 대상 처리
        existingProduct.images.filterNot { desiredSet.contains(it.fileName) }.forEach { imageService.delete(it.id) }

        // 새로운 이미지 목록 구성
        val updatedImages =
            finalImageNames.map { fileName ->
                existingImages[fileName] ?: createNewImageEntity(fileName, userId, existingProduct, newImageMappings)
            }
        existingProduct.images.clear()
        existingProduct.images.addAll(updatedImages)
    }

    private fun createNewImageEntity(
        fileName: String,
        userId: Long,
        product: ProductEntity,
        newImageMappings: Map<String, String>,
    ): ImageEntity {
        if (fileName in newImageMappings.values) {
            return ImageEntity.from(
                Image(
                    userId = userId,
                    fileName = fileName,
                ),
            ).apply { this.product = product }
        }
        throw IllegalArgumentException("Invalid image reference: $fileName")
    }

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
}
