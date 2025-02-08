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

// 플레이스홀더 접두어를 상수로 정의 (추후 공통 상수 파일로 분리 가능)
private const val PLACEHOLDER_PREFIX = "new_"

// 제품 관련 유스케이스 구현체
@Service
class ProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val imageService: ImageService,
) : ProductUseCase {
    // 제품 생성 유스케이스
    // 1. 전달받은 MultipartFile 목록을 업로드하여 Image 도메인 객체 리스트를 생성
    // 2. 생성된 Image 리스트를 포함해 CreateProductRequest를 통해 제품 도메인 모델을 구성
    // 3. 제품 도메인을 저장하고 옵션을 별도 저장한 후 최종 제품 정보를 응답 DTO로 반환한다.
    @Transactional
    override fun saveProduct(
        request: CreateProductRequest,
        images: List<MultipartFile>,
    ): ProductResponse {
        val uploadedImages: List<Image> = uploadImages(images, request.userId)
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
    // 프론트엔드에서는 이미지 정보를 ImageReference 값 객체의 리스트로 전달한다.
    // 1. 신규 MultipartFile 목록이 있으면 업로드 후, 각 이미지에 대해 플레이스홀더("new_0", "new_1", …)와 매핑한다.
    // 2. 업데이트 요청에 포함된 이미지 목록(프론트엔드에서는 List<ImageReference>)을
    //    문자열(identifier) 리스트로 변환한 후, resolveFinalImageOrder()를 호출하여 최종 Image 도메인 객체 리스트를 구성한다.
    // 3. UpdateProductRequest를 통해 제품 도메인 모델을 생성 및 검증하고, 기존 제품을 조회하여 엔티티로 변환한다.
    // 4. 기존 엔티티의 이미지 컬렉션을 최종 이미지 목록에 맞게 동기화(추가, 삭제, 재정렬)한 후,
    //    나머지 필드와 옵션을 업데이트하고 저장하여 응답 DTO로 반환한다.
    @Transactional
    override fun updateProduct(
        request: UpdateProductRequest,
        images: List<MultipartFile>?,
    ): ProductResponse {
        // 신규 업로드된 이미지가 있다면 업로드 후, PLACEHOLDER_PREFIX("new_")를 사용하여 "new_0", "new_1" 등의 키와 매핑한다.
        val newImageMappings: Map<String, Image> = uploadNewImagesWithPlaceholders(images, request.userId)
        // 프론트엔드에서 전달된 이미지 식별자는 ImageReference 타입이므로, 이를 문자열(identifier) 리스트로 변환한다.
        val requestedImageIdentifiers: List<String> = request.images.map { it.identifier }
        // 전달받은 식별자 리스트를 바탕으로 최종 Image 도메인 객체 리스트를 구성한다.
        // 만약 식별자가 newImageMappings에 존재하면 실제 업로드된 Image로 대체하고,
        // 그렇지 않으면 identifier를 URL로 간주하여 URL에서 fileName을 추출한 후 Image 객체를 생성한다.
        val finalImageOrder: List<Image> = resolveFinalImageOrder(requestedImageIdentifiers, newImageMappings, request.userId)
        // 업데이트 요청 데이터를 통해 제품 도메인 모델을 생성 및 검증한다.
        val productFromRequest: Product =
            UpdateProductRequest.toDomain(request, finalImageOrder)
                .copy(options = emptyList())
        productFromRequest.validateForUpdate()
        // 기존 제품 도메인을 조회하고, 없으면 예외 발생, 있으면 엔티티로 변환한다.
        val existingProductDomain: Product =
            productPersistencePort.findById(productFromRequest.productId)
                ?: throw IllegalArgumentException("Product not found: ${productFromRequest.productId}")
        val existingProductEntity: ProductEntity = ProductEntity.fromDomain(existingProductDomain)
        // 기존 엔티티의 이미지 컬렉션을 최종 이미지 목록에 맞게 동기화한다. (내부 고유 식별자인 fileName 기준 매핑)
        synchronizeProductImages(
            existingProduct = existingProductEntity,
            finalImages = finalImageOrder,
            newImageMappings = newImageMappings,
            userId = request.userId,
        )
        // 나머지 필드를 업데이트하고, 제품 옵션을 처리한 후 저장한다.
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
    // 헬퍼 메소드들

    // (1) 이미지 업로드
    // - 전달받은 MultipartFile 리스트를 SaveImageCommand 리스트로 변환하고 imageService.saveAll()로 업로드하여 이미지 ID 리스트를 획득한다.
    // - 각 이미지 ID마다 imageService.getOne()을 호출하여 GetImageResponse를 받고,
    //   이미지 서비스에서 반환한 url에서 파일명을 추출하여 Image 도메인 객체를 생성한다.
    // - 내부에는 fileName과 URL이 보존되어 업데이트 시 이미지 매핑 및 동기화에 활용된다.
    override fun uploadImages(
        images: List<MultipartFile>,
        userId: Long,
    ): List<Image> {
        val commands = images.map { file -> SaveImageCommand(file, userId) }
        val imageIds: List<Long> = imageService.saveAll(commands)
        return imageIds.map { id ->
            val response = imageService.getOne(id)
            // GetImageResponse에는 fileName 대신 url만 있으므로, url에서 fileName을 추출
            val fileName = extractFileNameFromUrl(response.url)
            Image(
                imageId = response.imageId,
                userId = userId,
                fileName = fileName,
                url = response.url,
            )
        }
    }

    // (2) 신규 이미지 업로드 후 플레이스홀더 매핑
    // - 신규 업로드된 이미지 리스트를 uploadImages()로 처리한 후,
    // - 각 이미지에 대해 PLACEHOLDER_PREFIX와 인덱스를 조합하여 "new_0", "new_1" 등으로 매핑한 Map<String, Image>를 반환한다.
    private fun uploadNewImagesWithPlaceholders(
        newImages: List<MultipartFile>?,
        userId: Long,
    ): Map<String, Image> {
        if (newImages.isNullOrEmpty()) return emptyMap()
        val uploaded: List<Image> = uploadImages(newImages, userId)
        return newImages.mapIndexed { idx, _ -> "$PLACEHOLDER_PREFIX$idx" to uploaded[idx] }.toMap()
    }

    // (3) 요청 이미지 목록을 최종 Image 도메인 객체 목록으로 변환
    // - 프론트엔드에서는 UpdateProductRequest의 images 필드로 ImageReference 타입의 리스트를 전달한다.
    // - 이를 문자열(identifier) 리스트로 변환한 후,
    //   만약 해당 identifier가 newImageMappings에 존재하면 실제 업로드된 Image로 대체하고,
    //   그렇지 않으면 identifier를 URL로 간주하여, URL에서 파일명을 추출한 후 Image 객체를 생성한다.
    private fun resolveFinalImageOrder(
        requestedImageIdentifiers: List<String>,
        newImageMappings: Map<String, Image>,
        userId: Long,
    ): List<Image> {
        return requestedImageIdentifiers.map { identifier ->
            if (identifier.contains("://")) {
                // identifier가 URL이면, URL에서 fileName 추출
                val fileName = extractFileNameFromUrl(identifier)
                Image(userId = userId, fileName = fileName, url = identifier)
            } else {
                // placeholder 등의 경우 처리
                newImageMappings[identifier] ?: Image(userId = userId, fileName = identifier, url = "")
            }
        }
    }

    // (4) 기존 엔티티의 이미지와 최종 이미지 목록을 비교하여 동기화
    // - 기존 ProductEntity의 images 컬렉션을 내부 고유 fileName을 기준으로 Map으로 구성하고,
    // - 최종 업데이트 요청에서 구성된 Image 목록의 fileName 집합을 추출한다.
    // - 업데이트 요청에 포함되지 않은 기존 이미지는 imageService.delete()를 호출하여 삭제하고,
    // - 최종 이미지 순서대로, 기존 이미지가 있으면 재사용하고 없으면 신규 ImageEntity를 생성하여 반환한다.
    private fun synchronizeProductImages(
        existingProduct: ProductEntity,
        finalImages: List<Image>,
        newImageMappings: Map<String, Image>,
        userId: Long,
    ) {
        val existingImages = existingProduct.images.associateBy { it.fileName }
        val desiredFileNames = finalImages.map { it.fileName }.toSet()
        existingProduct.images.filterNot { desiredFileNames.contains(it.fileName) }
            .forEach { imageService.delete(it.id) }
        val updatedImages =
            finalImages.map { image ->
                existingImages[image.fileName] ?: createNewImageEntity(image, userId, existingProduct, newImageMappings)
            }
        existingProduct.images.clear()
        existingProduct.images.addAll(updatedImages)
    }

    // (5) 신규 Image 도메인 객체를 ImageEntity로 변환
    // - 만약 newImageMappings에 해당 Image의 fileName과 일치하는 값이 있다면,
    //   ImageEntity.from(image)를 호출하여 신규 ImageEntity를 생성하고, product 참조를 설정하여 반환한다.
    private fun createNewImageEntity(
        image: Image,
        userId: Long,
        product: ProductEntity,
        newImageMappings: Map<String, Image>,
    ): ImageEntity {
        if (newImageMappings.values.any { it.fileName == image.fileName }) {
            return ImageEntity.from(image).apply { this.product = product }
        }
        throw IllegalArgumentException("Invalid image reference: ${image.fileName}")
    }

    // 제품 업데이트 요청 데이터를 기반으로 ProductEntity의 나머지 필드를 업데이트한다.
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

    // 제품 옵션 업데이트 또는 신규 저장을 처리한다.
    private fun processProductOption(
        dto: UpdateProductOptionRequest,
        product: Product,
    ) {
        val domainOption = UpdateProductOptionRequest.toDomain(dto, product.productId)
        if (domainOption.optionId != 0L) {
            val existingOption = productOptionPersistencePort.findById(domainOption.optionId)
            existingOption?.let {
                it.name = domainOption.name
                it.additionalPrice = domainOption.additionalPrice
                it.description = domainOption.description
                productOptionPersistencePort.save(it, product)
            }
        } else {
            productOptionPersistencePort.save(domainOption, product)
        }
    }

    // 제품 옵션 생성 요청에 따라 옵션을 저장한다.
    private fun saveProductOptions(
        product: Product,
        options: List<CreateProductOptionRequest>,
    ) {
        options.forEach { dto ->
            val domainOption = CreateProductOptionRequest.toDomain(dto, product.productId)
            productOptionPersistencePort.save(domainOption, product)
        }
    }

    // 가격 범위 검증: 최소값 및 최대값 조건을 확인한다.
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

    // 제품 생성 시, 동일 사용자에 동일 제목의 제품이 존재하지 않는지 검증한다.
    private fun validateForCreation(product: Product) {
        require(!productPersistencePort.existsByUserIdAndTitle(product.userId, product.title)) {
            "A product with the same title already exists: ${product.title}."
        }
    }

    // 삭제할 옵션 ID들을 순회하며 제품 옵션을 삭제한다.
    private fun handleDeletedOptions(toDelete: Set<Long>) {
        toDelete.forEach { optionId ->
            productOptionPersistencePort.deleteById(optionId)
        }
    }

    // 제품 도메인 모델에 옵션 데이터를 추가하여 최종 제품 정보를 보완한다.
    private fun enrichProduct(product: Product): Product {
        return product.copy(options = productOptionPersistencePort.findByProductId(product.productId))
    }

    // 헬퍼 메소드: URL에서 마지막 슬래시 뒤의 문자열을 추출하여 fileName으로 사용한다.
    private fun extractFileNameFromUrl(url: String): String {
        return url.substringAfterLast('/')
    }
}
