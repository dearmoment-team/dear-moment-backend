package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.adapter.out.persistence.ImageEmbeddable
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.application.dto.request.*
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

    // ImageHandler 인스턴스 생성(이미지 관련 로직 분리)
    private val imageHandler = ImageHandler(imageService)

    /**
     * 상품 생성
     */
    @Transactional
    override fun saveProduct(
        request: CreateProductRequest,
    ): ProductResponse {
        // 1) 대표 이미지(필수)
        val mainImg = request.mainImageFile?.let {
            imageService.uploadSingleImage(it, request.userId)
        } ?: throw IllegalArgumentException("대표 이미지는 필수입니다.")

        // 2) 서브 이미지 (최소 4장)
        val subImgs = request.subImageFiles.map {
            imageService.uploadSingleImage(it, request.userId)
        }
        if (subImgs.size != 4) {
            throw IllegalArgumentException("서브 이미지는 정확히 4장이어야 합니다. 현재 ${subImgs.size}장입니다.")
        }

        // 3) 추가 이미지 (최대 5장)
        val additionalImgs = request.additionalImageFiles.map {
            imageService.uploadSingleImage(it, request.userId)
        }
        if (additionalImgs.size > 5) {
            throw IllegalArgumentException("추가 이미지는 최대 5장까지만 가능합니다. 현재 ${additionalImgs.size}장입니다.")
        }

        // 4) DTO -> 도메인 변환
        val product: Product = CreateProductRequest.toDomain(
            req = request,
            mainImageUrl = mainImg.url,
            subImagesUrls = subImgs.map { it.url },
            additionalImagesUrls = additionalImgs.map { it.url },
        ).copy(options = emptyList())

        validateForCreation(product)

        // 5) 상품 저장
        val savedProduct: Product = productPersistencePort.save(product)

        // 6) 옵션 등록
        saveProductOptions(savedProduct, request.options)

        // 7) 옵션까지 채워서 최종 반환
        val completeProduct: Product = enrichProduct(savedProduct)
        return ProductResponse.fromDomain(completeProduct)
    }

    /**
     * 상품 수정
     * - 메인 이미지: 새 파일이 있으면 교체(기존 메인 이미지 삭제), 없으면 기존 유지
     * - 서브 이미지: 부분 업데이트(request.subImageUpdates) 또는 전체 교체(request.subImageFiles), 무조건 4장
     * - 추가 이미지: 부분 업데이트(request.additionalImageUpdates) 또는 전체 교체(request.additionalImageFiles), 최대 5장
     * - 옵션: 전체 교체 방식
     */
    @Transactional
    override fun updateProduct(
        request: UpdateProductRequest,
    ): ProductResponse {
        // 1) 기존 상품 조회
        val existingProduct: Product =
            productPersistencePort.findById(request.productId)
                ?: throw IllegalArgumentException("존재하지 않는 상품 ID: ${request.productId}")

        // 2) 업로드 처리 (서브, 추가 이미지는 미리 업로드)
        val newSubImgs: List<Image> = request.subImageFiles?.map {
            imageService.uploadSingleImage(it, request.userId)
        } ?: emptyList()

        val newAdditionalImgs: List<Image> = request.additionalImageFiles?.map {
            imageService.uploadSingleImage(it, request.userId)
        } ?: emptyList()

        // 3) 메인 이미지 업데이트: 새 파일이 있을 경우 기존 메인 이미지 삭제 후 교체
        val mainImageUrl = if (request.mainImageFile != null) {
            imageHandler.updateMainImage(request.mainImageFile, request.userId, existingProduct.mainImage).url
        } else {
            existingProduct.mainImage.url
        }

        // 4) 서브 이미지 처리
        val updatedSubImages: List<ImageEmbeddable> = imageHandler.processSubImages(
            currentSubImages = existingProduct.subImages
                .map { ImageEmbeddable.fromDomainImage(it) }
                .toMutableList(),
            updates = if (request.subImageUpdates.isEmpty()) null else request.subImageUpdates,
            newUploadedImages = if (newSubImgs.isEmpty()) null else newSubImgs,
            userId = request.userId
        )

        // 5) 추가 이미지 처리
        val updatedAdditionalImages: List<ImageEmbeddable> = imageHandler.processAdditionalImages(
            currentAdditionalImages = existingProduct.additionalImages
                .map { ImageEmbeddable.fromDomainImage(it) }
                .toMutableList(),
            updates = request.additionalImageUpdates.ifEmpty { null },
            newUploadedImages = newAdditionalImgs.ifEmpty { null },
            userId = request.userId,
            maxCount = 5
        )


        // 6) 기존 도메인과 결합하여 DTO -> 도메인 변환 (옵션 제외)
        val productFromReq = UpdateProductRequest.toDomain(
            req = request,
            mainImageUrl = mainImageUrl,
            subImagesUrls = updatedSubImages.map { it.url },
            additionalImagesUrls = updatedAdditionalImages.map { it.url }
        ).copy(options = emptyList())

        productFromReq.validateForUpdate()

        // 7) 엔티티 업데이트 (기본 필드)
        val existingEntity = ProductEntity.fromDomain(existingProduct)
        updateProductEntity(existingEntity, productFromReq)

        // 8) 반영된 이미지 업데이트 적용
        existingEntity.subImages.clear()
        existingEntity.subImages.addAll(updatedSubImages)

        existingEntity.additionalImages.clear()
        existingEntity.additionalImages.addAll(updatedAdditionalImages)

        // 9) 옵션 처리 (전체 교체)
        val existingOptions = productOptionPersistencePort.findByProductId(existingProduct.productId)
        val updateOptionIds = request.options
            .filter { it.optionId != null && it.optionId != 0L }
            .mapNotNull { it.optionId }
            .toSet()

        existingOptions.filter { it.optionId !in updateOptionIds }
            .forEach { opt -> productOptionPersistencePort.deleteById(opt.optionId) }

        request.options.forEach { dto ->
            processUpdateOption(dto, existingProduct.productId)
        }

        // 10) 최종 저장 및 결과 반환
        val updatedDomain = productPersistencePort.save(existingEntity.toDomain())
        val completeProduct = enrichProduct(updatedDomain)
        return ProductResponse.fromDomain(completeProduct)
    }

    /**
     * 상품 삭제
     */
    @Transactional
    override fun deleteProduct(productId: Long) {
        val product: Product =
            productPersistencePort.findById(productId)
                ?: throw IllegalArgumentException("삭제할 상품이 존재하지 않습니다. ID: $productId")
        // 연관된 이미지 모두 삭제
        val allImages = listOf(product.mainImage) + product.subImages + product.additionalImages
        allImages.forEach { imageService.delete(it.imageId) }
        productOptionPersistencePort.deleteAllByProductId(productId)
        productPersistencePort.deleteById(productId)
    }

    /**
     * 상품 단건 조회
     */
    override fun getProductById(productId: Long): ProductResponse {
        val product = productPersistencePort.findById(productId)
            ?: throw IllegalArgumentException("상품을 찾을 수 없습니다. ID: $productId")
        val completeProduct = enrichProduct(product)
        return ProductResponse.fromDomain(completeProduct)
    }

    /**
     * 검색(페이징)
     */
    override fun searchProducts(
        title: String?,
        productType: String?,
        shootingPlace: String?,
        sortBy: String?,
        page: Int,
        size: Int,
    ): PagedResponse<ProductResponse> {
        val found = productPersistencePort.searchByCriteria(
            title = title,
            productType = productType,
            shootingPlace = shootingPlace,
            sortBy = sortBy,
        )
        val mockData = found.mapIndexed { idx, product -> Pair(product, idx + 1) }
        val sortedProducts = when (sortBy) {
            "likes" -> mockData.sortedByDescending { it.second }.map { it.first }
            "created-desc" -> found.sortedByDescending { it.createdAt }
            else -> mockData.map { it.first }
        }
        val totalElements = sortedProducts.size.toLong()
        val totalPages = if (size > 0) ((sortedProducts.size + size - 1) / size) else 0
        val fromIndex = page * size
        val toIndex = min(fromIndex + size, sortedProducts.size)
        val pageContent =
            if (fromIndex >= sortedProducts.size) emptyList() else sortedProducts.subList(fromIndex, toIndex)
        return PagedResponse(
            content = pageContent.map { ProductResponse.fromDomain(it) },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }

    /**
     * 메인 페이지 노출용(페이징)
     */
    override fun getMainPageProducts(page: Int, size: Int): PagedResponse<ProductResponse> {
        val all = productPersistencePort.findAll()
        val mockData = all.mapIndexed { idx, product -> Pair(product, idx + 1) }
        val sortedProducts = mockData.sortedByDescending { it.second }.map { it.first }
        val totalElements = sortedProducts.size.toLong()
        val totalPages = ceil(sortedProducts.size.toDouble() / size).toInt()
        val fromIndex = page * size
        val toIndex = min(fromIndex + size, sortedProducts.size)
        val pageContent =
            if (fromIndex >= sortedProducts.size) emptyList() else sortedProducts.subList(fromIndex, toIndex)
        return PagedResponse(
            content = pageContent.map { ProductResponse.fromDomain(it) },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }

    // 내부 헬퍼 메서드

    private fun saveProductOptions(
        product: Product,
        options: List<CreateProductOptionRequest>,
    ) {
        options.forEach { dto ->
            val domainOption = CreateProductOptionRequest.toDomain(dto, product.productId)
            productOptionPersistencePort.save(domainOption, product)
        }
    }

    private fun processUpdateOption(
        dto: UpdateProductOptionRequest,
        productId: Long,
    ) {
        val domainOption = UpdateProductOptionRequest.toDomain(dto, productId)
        if (domainOption.optionId != 0L) {
            val existingOption = productOptionPersistencePort.findById(domainOption.optionId)
            val updatedOption = existingOption.copy(
                name = domainOption.name,
                optionType = domainOption.optionType,
                discountAvailable = domainOption.discountAvailable,
                originalPrice = domainOption.originalPrice,
                discountPrice = domainOption.discountPrice,
                description = domainOption.description,
                costumeCount = domainOption.costumeCount,
                shootingLocationCount = domainOption.shootingLocationCount,
                shootingHours = domainOption.shootingHours,
                shootingMinutes = domainOption.shootingMinutes,
                retouchedCount = domainOption.retouchedCount,
                originalProvided = domainOption.originalProvided,
                partnerShops = domainOption.partnerShops,
            )
            val product = productPersistencePort.findById(productId)
                ?: throw IllegalArgumentException("상품이 존재하지 않습니다. productId=$productId")
            productOptionPersistencePort.save(updatedOption, product)
        } else {
            val product = productPersistencePort.findById(productId)
                ?: throw IllegalArgumentException("상품이 존재하지 않습니다. productId=$productId")
            productOptionPersistencePort.save(domainOption, product)
        }
    }

    private fun updateProductEntity(
        entity: ProductEntity,
        domain: Product,
    ) {
        with(entity) {
            userId = domain.userId
            productType = domain.productType
            shootingPlace = domain.shootingPlace
            title = domain.title
            description = domain.description.takeIf { it.isNotBlank() }
            availableSeasons.clear()
            availableSeasons.addAll(domain.availableSeasons)
            cameraTypes.clear()
            cameraTypes.addAll(domain.cameraTypes)
            retouchStyles.clear()
            retouchStyles.addAll(domain.retouchStyles)
            detailedInfo = domain.detailedInfo.takeIf { it.isNotBlank() }
            contactInfo = domain.contactInfo.takeIf { it.isNotBlank() }
        }
    }

    private fun validateForCreation(product: Product) {
        require(!productPersistencePort.existsByUserIdAndTitle(product.userId, product.title)) {
            "동일 제목의 상품이 이미 존재합니다: ${product.title}"
        }
    }

    private fun enrichProduct(product: Product): Product {
        val opts = productOptionPersistencePort.findByProductId(product.productId)
        return product.copy(options = opts)
    }
}

/**
 * 이미지 관련 다양한 업데이트/삭제/추가 기능을 처리하는 헬퍼 클래스
 */
class ImageHandler(private val imageService: ImageService) {

    /**
     * 메인 이미지 업데이트:
     * - 새로운 파일이 있다면 새 이미지 업로드 후 기존 이미지를 삭제하고 새 이미지를 반환합니다.
     */
    fun updateMainImage(newFile: MultipartFile, userId: Long, currentImage: Image): Image {
        val newImage = imageService.uploadSingleImage(newFile, userId)
        imageService.delete(currentImage.imageId)
        return newImage
    }

    /**
     * 서브 이미지 처리:
     * - 부분 업데이트 요청(updates)이 있으면 이를 처리합니다.
     * - 그렇지 않고 새로 업로드된 이미지(newUploadedImages)가 있다면 전체 교체합니다.
     * - 만약 둘 다 없으면 기존 이미지를 그대로 반환합니다.
     * - 최종 이미지 개수가 4개 미만이면 예외를 발생시킵니다.
     */
    fun processSubImages(
        currentSubImages: MutableList<ImageEmbeddable>,
        updates: List<SubImageUpdateRequest>?,
        newUploadedImages: List<Image>?,
        userId: Long,
    ): List<ImageEmbeddable> {
        if (!updates.isNullOrEmpty()) {
            val result = currentSubImages.toMutableList()
            updates.forEach { update ->
                when {
                    update.isDeleted && update.imageId != null -> {
                        imageService.delete(update.imageId)
                        result.removeIf { it.imageId == update.imageId }
                    }

                    update.newImageFile != null && update.imageId != null -> {
                        val uploaded = imageService.uploadSingleImage(update.newImageFile, userId)
                        val embeddable = ImageEmbeddable.fromDomainImage(uploaded)
                        result.replaceAll { if (it.imageId == update.imageId) embeddable else it }
                    }

                    update.imageId == null && !update.isDeleted -> {
                        val uploaded = imageService.uploadSingleImage(update.newImageFile!!, userId)
                        result.add(ImageEmbeddable.fromDomainImage(uploaded))
                    }
                }
            }
            if (result.size != 4) {
                throw IllegalArgumentException("서브 이미지는 정확히 4장이어야 합니다. 현재 ${result.size}장입니다.")
            }
            return result
        } else if (!newUploadedImages.isNullOrEmpty()) {
            if (newUploadedImages.size != 4) {
                throw IllegalArgumentException("서브 이미지는 정확히 4장이어야 합니다. 현재 ${newUploadedImages.size}장입니다.")
            }
            return newUploadedImages.map { ImageEmbeddable.fromDomainImage(it) }
        }
        if (currentSubImages.size != 4) {
            throw IllegalArgumentException("서브 이미지는 정확히 4장이어야 합니다. 현재 ${currentSubImages.size}장입니다.")
        }
        return currentSubImages
    }

    /**
     * 추가 이미지 처리:
     * - 부분 업데이트 요청(updates)이 있으면 이를 처리합니다.
     * - 그렇지 않고 새로 업로드된 이미지(newUploadedImages)가 있다면 전체 교체합니다.
     * - 만약 둘 다 없으면 기존 이미지를 그대로 반환합니다.
     * - 최종 이미지 개수가 maxCount를 초과하면 예외를 발생시킵니다.
     */
    fun processAdditionalImages(
        currentAdditionalImages: MutableList<ImageEmbeddable>,
        updates: List<SubImageUpdateRequest>?,
        newUploadedImages: List<Image>?,
        userId: Long,
        maxCount: Int = 5
    ): List<ImageEmbeddable> {
        if (updates != null && updates.isNotEmpty()) {
            val result = currentAdditionalImages.toMutableList()
            updates.forEach { update ->
                when {
                    update.isDeleted && update.imageId != null -> {
                        imageService.delete(update.imageId)
                        result.removeIf { it.imageId == update.imageId }
                    }

                    update.newImageFile != null && update.imageId != null -> {
                        val uploaded = imageService.uploadSingleImage(update.newImageFile, userId)
                        val embeddable = ImageEmbeddable.fromDomainImage(uploaded)
                        result.replaceAll { if (it.imageId == update.imageId) embeddable else it }
                    }

                    update.imageId == null && !update.isDeleted -> {
                        val uploaded = imageService.uploadSingleImage(update.newImageFile!!, userId)
                        result.add(ImageEmbeddable.fromDomainImage(uploaded))
                    }
                }
            }
            if (result.size > maxCount) {
                throw IllegalArgumentException("추가 이미지는 최대 $`maxCount`장까지만 가능합니다. 현재 ${result.size}장입니다.")
            }
            return result
        } else if (!newUploadedImages.isNullOrEmpty()) {
            if (newUploadedImages.size > maxCount) {
                throw IllegalArgumentException("추가 이미지는 최대 $`maxCount`장까지만 가능합니다. 현재 ${newUploadedImages.size}장입니다.")
            }
            return newUploadedImages.map { ImageEmbeddable.fromDomainImage(it) }
        }
        return currentAdditionalImages
    }
}
