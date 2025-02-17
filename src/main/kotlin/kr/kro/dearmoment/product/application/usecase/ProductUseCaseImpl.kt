package kr.kro.dearmoment.product.application.usecase

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
    // ProductImageService는 이미지 순서 결정 및 동기화만 수행
    private val productImageService: ProductImageService,
    // 실제 이미지 업로드 및 저장은 ImageService에서 수행
    private val imageService: ImageService,
) : ProductUseCase {

    // 제품 생성 유스케이스
    @Transactional
    override fun saveProduct(
        request: CreateProductRequest,
        images: List<MultipartFile>,
    ): ProductResponse {
        // 이미지 업로드: ImageService를 이용하여 이미지 저장 후 Image 도메인 객체 생성
        val commands = images.map { file -> SaveImageCommand(file, request.userId) }
        val imageIds: List<Long> = imageService.saveAll(commands)
        val uploadedImages: List<Image> = imageIds.map { id ->
            val response = imageService.getOne(id)
            val fileName = response.url.substringAfterLast('/')
            Image(
                imageId = response.imageId,
                userId = request.userId,
                fileName = fileName,
                url = response.url,
            )
        }

        // 생성 요청을 도메인 모델로 변환
        val product: Product = CreateProductRequest.toDomain(request, uploadedImages)
            .copy(options = emptyList())
        validateForCreation(product)
        val savedProduct: Product = productPersistencePort.save(product)
        saveProductOptions(savedProduct, request.options)
        val completeProduct: Product = enrichProduct(savedProduct)
        return ProductResponse.fromDomain(completeProduct)
    }

    // 제품 업데이트 유스케이스
    @Transactional
    override fun updateProduct(
        request: UpdateProductRequest,
        images: List<MultipartFile>?,
    ): ProductResponse {
        // 신규 이미지 업로드 처리: images가 존재하면 ImageService를 통해 업로드 후,
        // "new_0", "new_1", ... 형태의 매핑 Map을 생성한다.
        val newImageMappings: Map<String, Image> = if (!images.isNullOrEmpty()) {
            val commands = images.map { file -> SaveImageCommand(file, request.userId) }
            val imageIds = imageService.saveAll(commands)
            imageIds.mapIndexed { idx, id ->
                val response = imageService.getOne(id)
                val fileName = response.url.substringAfterLast('/')
                "new_$idx" to Image(
                    imageId = response.imageId,
                    userId = request.userId,
                    fileName = fileName,
                    url = response.url,
                )
            }.toMap()
        } else {
            emptyMap()
        }

        // 프론트엔드에서 전달받은 이미지 식별자 목록과 업로드된 신규 이미지 매핑을 이용해 최종 이미지 순서를 결정
        val requestedImageIdentifiers: List<String> = request.images.map { it.identifier }
        val finalImageOrder: List<Image> = productImageService.resolveFinalImageOrder(
            requestedImageIdentifiers,
            newImageMappings,
            request.userId,
        )

        // UpdateProductRequest를 도메인 모델로 변환 (옵션은 빈 리스트로 초기화)
        val productFromRequest: Product = UpdateProductRequest.toDomain(request, finalImageOrder)
            .copy(options = emptyList())
        productFromRequest.validateForUpdate()

        // 기존 상품 조회 및 엔티티 변환
        val existingProductDomain: Product = productPersistencePort.findById(productFromRequest.productId)
            ?: throw IllegalArgumentException("Product not found: ${productFromRequest.productId}")
        val existingProductEntity: ProductEntity = ProductEntity.fromDomain(existingProductDomain)

        // 이미지 동기화: 기존 상품 엔티티의 이미지 목록과 최종 이미지 목록을 동기화
        productImageService.synchronizeProductImages(
            existingProductEntity,
            finalImageOrder,
            newImageMappings,
            request.userId,
        )

        // 상품 엔티티의 나머지 필드 업데이트
        updateProductEntity(existingProductEntity, request)

        // 옵션 처리: 기존 옵션 중 업데이트 대상이 아닌 옵션 삭제, 신규/수정 옵션 처리
        val existingOptions = productOptionPersistencePort.findByProductId(productFromRequest.productId)
        val updateOptionIds = request.options
            .filter { it.optionId != null && it.optionId != 0L }
            .map { it.optionId }
            .toSet()
        existingOptions.filter { it.optionId !in updateOptionIds }
            .forEach { productOptionPersistencePort.deleteById(it.optionId) }
        request.options.forEach { dto ->
            processProductOption(dto, productFromRequest)
        }

        // 변경된 엔티티 저장 후 enrich 처리하여 최종 응답 DTO 생성
        val updatedDomain: Product = productPersistencePort.save(existingProductEntity.toDomain())
        val completeProduct: Product = enrichProduct(updatedDomain)
        return ProductResponse.fromDomain(completeProduct)
    }

    @Transactional
    override fun deleteProduct(productId: Long) {
        // 제품 전체 정보를 조회해서 이미지 리스트를 가져옵니다.
        val product: Product = productPersistencePort.findById(productId)
            ?: throw IllegalArgumentException("The product to delete does not exist: $productId.")

        // 제품에 연결된 모든 이미지 삭제 (ImageService를 통해)
        product.images.forEach { image ->
            imageService.delete(image.imageId)
        }

        // 관련 옵션 삭제 후, 제품 삭제
        productOptionPersistencePort.deleteAllByProductId(productId)
        productPersistencePort.deleteById(productId)
    }

    override fun getProductById(productId: Long): ProductResponse {
        val product: Product = productPersistencePort.findById(productId)
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
        val result: List<Product> = productPersistencePort.searchByCriteria(
            title = title,
            priceRange = minPrice?.let { Pair(it, maxPrice) },
            typeCode = typeCode,
            sortBy = sortBy,
        )
        val mockData = result.mapIndexed { index, product -> Pair(product, index + 1) }
        val sortedProducts = when (sortBy) {
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
        val result: List<Product> = productPersistencePort.searchByCriteria(
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
    // 헬퍼 메소드들: 상품 엔티티 업데이트, 옵션 처리 및 검증

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
            shootingTime = domain.shootingTimeMinutes?.let { java.time.Duration.ofMinutes(it.toLong()) }
            shootingLocation = domain.shootingLocation
            numberOfCostumes = domain.numberOfCostumes
            seasonYear = domain.seasonYear
            seasonHalf = domain.seasonHalf
            partnerShops = domain.partnerShops.map { ps ->
                PartnerShopEmbeddable(ps.name, ps.link)
            }
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
            val updatedOption = existingOption.copy(
                name = domainOption.name,
                additionalPrice = domainOption.additionalPrice,
                description = domainOption.description,
            )
            productOptionPersistencePort.save(updatedOption, product)
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

    private fun enrichProduct(product: Product): Product {
        return product.copy(options = productOptionPersistencePort.findByProductId(product.productId))
    }
}
