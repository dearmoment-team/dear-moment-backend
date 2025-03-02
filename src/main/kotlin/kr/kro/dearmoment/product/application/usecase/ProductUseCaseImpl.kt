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
import org.springframework.stereotype.Component
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
    private val imageHandler: ImageHandler
) : ProductUseCase {

    /**
     * 상품 생성 로직 (기존과 동일)
     */
    @Transactional
    override fun saveProduct(request: CreateProductRequest): ProductResponse {
        // 서브 이미지 / 추가 이미지 개수 검증
        if (request.subImageFiles.size != 4) {
            throw IllegalArgumentException(
                "서브 이미지는 정확히 4장이어야 합니다. 현재 ${request.subImageFiles.size}장입니다."
            )
        }
        if (request.additionalImageFiles.size > 5) {
            throw IllegalArgumentException(
                "추가 이미지는 최대 5장까지만 가능합니다. 현재 ${request.additionalImageFiles.size}장입니다."
            )
        }

        // 메인 이미지 업로드
        val mainImg = imageService.uploadSingleImage(
            request.mainImageFile ?: throw IllegalArgumentException("메인 이미지는 필수입니다."),
            request.userId
        )
        // 서브 이미지 업로드 (정확히 4장)
        val subImgs = request.subImageFiles.map {
            imageService.uploadSingleImage(it, request.userId)
        }
        // 추가 이미지 업로드 (최대 5장)
        val additionalImgs = request.additionalImageFiles.map {
            imageService.uploadSingleImage(it, request.userId)
        }

        // 도메인 객체 생성
        val product = CreateProductRequest.toDomain(
            req = request,
            mainImageUrl = mainImg.url,
            subImagesUrls = subImgs.map { it.url },
            additionalImagesUrls = additionalImgs.map { it.url }
        )

        // 동일 제목 중복 체크
        validateForCreation(product)

        // 상품 저장
        val savedProduct = productPersistencePort.save(product)

        // 옵션 생성
        saveProductOptions(savedProduct, request.options)

        return ProductResponse.fromDomain(enrichProduct(savedProduct))
    }

    /**
     * 상품 수정
     */
    @Transactional
    override fun updateProduct(request: UpdateProductRequest): ProductResponse {
        // 1) 기존 상품 조회
        val existingProduct = productPersistencePort.findById(request.productId)
            ?: throw IllegalArgumentException("존재하지 않는 상품 ID: ${request.productId}")

        // 2) 메인 이미지 교체 (새 파일이 있으면 교체, 없으면 기존 유지)
        val newMainImage: Image = request.mainImageFile?.let { file ->
            imageHandler.updateMainImage(file, request.userId, existingProduct.mainImage)
        } ?: existingProduct.mainImage

        // 3) 서브 이미지 처리 (최종 4장) -> List<Image>
        val updatedSubImages: List<Image> = imageHandler.processSubImagesFinal(
            currentSubImages = existingProduct.subImages,
            finalRequests = request.subImagesFinal,
            userId = request.userId
        )

        // 4) 추가 이미지 처리 (0~5장) -> List<Image>
        val updatedAdditionalImages: List<Image> = imageHandler.processAdditionalImagesFinal(
            currentAdditionalImages = existingProduct.additionalImages,
            finalRequests = request.additionalImagesFinal,
            userId = request.userId
        )

        // 5) 도메인 객체 생성 (이미 Image 객체를 직접 넣어줌)
        val productFromReq = UpdateProductRequest.toDomain(
            req = request,
            mainImage = newMainImage,
            subImages = updatedSubImages,
            additionalImages = updatedAdditionalImages
        )

        productFromReq.validateForUpdate()

        // 6) 기존 Entity를 가져와서 필요한 필드 반영
        val existingEntity = ProductEntity.fromDomain(existingProduct)
        updateProductEntity(existingEntity, productFromReq)

        // 서브/추가 이미지를 Embeddable 로 변환 후 저장
        existingEntity.subImages = updatedSubImages
            .map { ImageEmbeddable.fromDomainImage(it) }
            .toMutableList()

        existingEntity.additionalImages = updatedAdditionalImages
            .map { ImageEmbeddable.fromDomainImage(it) }
            .toMutableList()

        // 7) 옵션 동기화
        synchronizeOptions(existingProduct, request.options)

        // 8) DB 저장 후 반환
        val updatedEntity = productPersistencePort.save(existingEntity.toDomain())
        return ProductResponse.fromDomain(enrichProduct(updatedEntity))
    }

    /**
     * 옵션 동기화 로직(기존과 동일)
     */
    private fun synchronizeOptions(existingProduct: Product, requestOptions: List<UpdateProductOptionRequest>) {
        val existingOptions = productOptionPersistencePort.findByProductId(existingProduct.productId)
        val existingOptionMap = existingOptions.associateBy { it.optionId }

        val requestedIds = mutableSetOf<Long>()
        requestOptions.forEach { dto ->
            val optId = dto.optionId ?: 0L
            if (optId != 0L && existingOptionMap.containsKey(optId)) {
                // 기존 옵션 업데이트
                val existingOpt = existingOptionMap[optId]!!
                val updatedOpt = existingOpt.copy(
                    name = dto.name,
                    optionType = dto.optionType.let { kr.kro.dearmoment.product.domain.model.OptionType.valueOf(it) },
                    discountAvailable = dto.discountAvailable,
                    originalPrice = dto.originalPrice,
                    discountPrice = dto.discountPrice,
                    description = dto.description ?: "",
                    costumeCount = dto.costumeCount,
                    shootingLocationCount = dto.shootingLocationCount,
                    shootingHours = dto.shootingHours,
                    shootingMinutes = dto.shootingMinutes,
                    retouchedCount = dto.retouchedCount,
                    originalProvided = dto.originalProvided,
                    partnerShops = dto.partnerShops.map {
                        kr.kro.dearmoment.product.domain.model.PartnerShop(
                            category = kr.kro.dearmoment.product.domain.model.PartnerShopCategory.valueOf(it.category),
                            name = it.name,
                            link = it.link
                        )
                    }
                )
                productOptionPersistencePort.save(updatedOpt, existingProduct)
                requestedIds.add(optId)
            } else {
                // 신규 옵션
                val newOpt = UpdateProductOptionRequest.toDomain(dto, existingProduct.productId)
                productOptionPersistencePort.save(newOpt, existingProduct)
            }
        }

        val toDelete = existingOptions.filter { !requestedIds.contains(it.optionId) }
        toDelete.forEach { opt -> productOptionPersistencePort.deleteById(opt.optionId) }
    }

    /**
     * 상품 삭제
     */
    @Transactional
    override fun deleteProduct(productId: Long) {
        val product = productPersistencePort.findById(productId)
            ?: throw IllegalArgumentException("삭제할 상품이 존재하지 않습니다. ID: $productId")

        // 이미지 삭제
        (listOf(product.mainImage) + product.subImages + product.additionalImages).forEach {
            imageService.delete(it.imageId)
        }
        productPersistencePort.deleteById(productId)
    }

    /**
     * 상품 단건 조회
     */
    override fun getProductById(productId: Long): ProductResponse {
        val product = productPersistencePort.findById(productId)
            ?: throw IllegalArgumentException("상품을 찾을 수 없습니다. ID: $productId")
        return ProductResponse.fromDomain(enrichProduct(product))
    }

    /**
     * 검색 (페이징)
     */
    override fun searchProducts(
        title: String?,
        productType: String?,
        shootingPlace: String?,
        sortBy: String?,
        page: Int,
        size: Int
    ): PagedResponse<ProductResponse> {
        val found = productPersistencePort.searchByCriteria(title, productType, shootingPlace, sortBy)
        val sorted = when (sortBy) {
            "created-desc" -> found.sortedByDescending { it.productId }
            else -> found
        }
        return createPagedResponse(sorted, page, size)
    }

    /**
     * 메인 페이지 (페이징)
     */
    override fun getMainPageProducts(page: Int, size: Int): PagedResponse<ProductResponse> {
        val all = productPersistencePort.findAll()
        val mockData = all.mapIndexed { idx, product -> Pair(product, idx + 1) }
        val sortedProducts = mockData.sortedByDescending { it.second }.map { it.first }

        val totalElements = sortedProducts.size.toLong()
        val totalPages = ceil(totalElements / size.toDouble()).toInt()
        val fromIndex = page * size
        val toIndex = min(fromIndex + size, sortedProducts.size)
        val pageContent =
            if (fromIndex >= sortedProducts.size) emptyList() else sortedProducts.subList(fromIndex, toIndex)

        return PagedResponse(
            content = pageContent.map { ProductResponse.fromDomain(it) },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages
        )
    }

    // -----------------------------------
    // 내부 헬퍼들

    private fun validateForCreation(product: Product) {
        require(!productPersistencePort.existsByUserIdAndTitle(product.userId, product.title)) {
            "동일 제목의 상품이 이미 존재합니다: ${product.title}"
        }
    }

    private fun enrichProduct(product: Product): Product {
        val opts = productOptionPersistencePort.findByProductId(product.productId)
        return product.copy(options = opts)
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

    private fun createPagedResponse(
        sorted: List<Product>,
        page: Int,
        size: Int
    ): PagedResponse<ProductResponse> {
        val totalElements = sorted.size.toLong()
        val totalPages = ceil(totalElements / size.toDouble()).toInt()
        val fromIndex = page * size
        val toIndex = min(fromIndex + size, sorted.size)
        val pageContent = if (fromIndex >= sorted.size) emptyList() else sorted.subList(fromIndex, toIndex)

        return PagedResponse(
            content = pageContent.map { ProductResponse.fromDomain(it) },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages
        )
    }
}


@Component
class ImageHandler(
    private val imageService: ImageService
) {

    /**
     * 메인 이미지 교체
     * - 파일이 있으면 새 업로드 후 기존 삭제
     * - 없으면 그대로
     */
    fun updateMainImage(newFile: MultipartFile, userId: Long, currentImage: Image): Image {
        val newImage = imageService.uploadSingleImage(newFile, userId)
        imageService.delete(currentImage.imageId)
        return newImage
    }

    /**
     * 서브이미지 최종 처리 (정확히 4장)
     *
     * @param currentSubImages 기존 서브 이미지 (도메인 객체)
     * @param finalRequests    최종 서브이미지(4장) 요청
     * @return 최종 4장의 [Image] 목록
     */
    fun processSubImagesFinal(
        currentSubImages: List<Image>,
        finalRequests: List<SubImageFinalRequest>,
        userId: Long
    ): List<Image> {
        if (finalRequests.size != 4) {
            throw IllegalArgumentException("서브 이미지는 정확히 4장이어야 합니다. 현재 ${finalRequests.size}장입니다.")
        }

        val currentMap = currentSubImages.associateBy { it.imageId }
        val result = mutableListOf<Image>()

        finalRequests.forEach { req ->
            when {
                req.imageId != null && req.file == null -> {
                    // 기존 이미지 유지
                    val existingImg = currentMap[req.imageId]
                        ?: throw IllegalArgumentException("기존 이미지 ID가 잘못되었습니다. imageId=${req.imageId}")
                    result.add(existingImg)
                }

                req.imageId != null && req.file != null -> {
                    // 기존 이미지 교체
                    val existingImg = currentMap[req.imageId]
                        ?: throw IllegalArgumentException("기존 이미지 ID가 잘못되었습니다. imageId=${req.imageId}")
                    val newImg = imageService.uploadSingleImage(req.file, userId)
                    imageService.delete(existingImg.imageId)
                    result.add(newImg)
                }

                req.imageId == null && req.file != null -> {
                    // 새 이미지 추가
                    val newImg = imageService.uploadSingleImage(req.file, userId)
                    result.add(newImg)
                }

                else -> {
                    // imageId, file 둘 다 null인 경우
                    throw IllegalArgumentException("서브이미지 요청에 잘못된 항목이 있습니다.")
                }
            }
        }

        // 기존 이미지 중 새 목록에 없는 것 삭제
        val newIds = result.map { it.imageId }.toSet()
        currentSubImages.forEach { old ->
            if (!newIds.contains(old.imageId)) {
                imageService.delete(old.imageId)
            }
        }

        return result
    }

    /**
     * 추가이미지 최종 처리 (0~5장)
     *
     * @param currentAdditionalImages 기존 추가이미지(도메인 객체)
     * @param finalRequests           최종 추가이미지 배열(0~5장)
     */
    fun processAdditionalImagesFinal(
        currentAdditionalImages: List<Image>,
        finalRequests: List<AdditionalImageFinalRequest>,
        userId: Long,
        maxCount: Int = 5
    ): List<Image> {
        if (finalRequests.size > maxCount) {
            throw IllegalArgumentException("추가 이미지는 최대 $maxCount 장까지만 가능합니다. 현재 ${finalRequests.size}장입니다.")
        }

        val currentMap = currentAdditionalImages.associateBy { it.imageId }
        val result = mutableListOf<Image>()

        finalRequests.forEach { req ->
            when {
                req.imageId != null && req.file == null -> {
                    // 기존 이미지 유지
                    val existingImg = currentMap[req.imageId]
                        ?: throw IllegalArgumentException("잘못된 추가이미지 ID입니다. imageId=${req.imageId}")
                    result.add(existingImg)
                }

                req.imageId != null && req.file != null -> {
                    // 기존 이미지 교체
                    val existingImg = currentMap[req.imageId]
                        ?: throw IllegalArgumentException("잘못된 추가이미지 ID입니다. imageId=${req.imageId}")
                    val newImg = imageService.uploadSingleImage(req.file, userId)
                    imageService.delete(existingImg.imageId)
                    result.add(newImg)
                }

                req.imageId == null && req.file != null -> {
                    // 새 이미지 추가
                    val newImg = imageService.uploadSingleImage(req.file, userId)
                    result.add(newImg)
                }

                else -> {
                    throw IllegalArgumentException("추가이미지 요청에 잘못된 항목이 있습니다.")
                }
            }
        }

        // 기존 이미지 중 새 목록에 없는 것 삭제
        val newIds = result.map { it.imageId }.toSet()
        currentAdditionalImages.forEach { old ->
            if (!newIds.contains(old.imageId)) {
                imageService.delete(old.imageId)
            }
        }

        return result
    }
}