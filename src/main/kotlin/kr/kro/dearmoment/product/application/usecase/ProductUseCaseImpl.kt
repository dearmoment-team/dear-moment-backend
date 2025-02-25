package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.adapter.out.persistence.ImageEmbeddable
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

    /**
     * 상품 생성
     */
    @Transactional
    override fun saveProduct(
        request: CreateProductRequest,
    ): ProductResponse {
        // 1) 이미지 업로드 (대표 1장, 서브 4장, 추가 최대 5장)
        val mainImg = request.mainImageFile?.let {
            uploadSingleImage(it, request.userId)
        } ?: throw IllegalArgumentException("대표 이미지는 필수입니다.")

        val subImgs = request.subImageFiles.map {
            uploadSingleImage(it, request.userId)
        }
        // 추가 이미지(최대 5장) - 이미 DTO에서 @Size(max=5)로 검증됨
        val additionalImgs = request.additionalImageFiles.map {
            uploadSingleImage(it, request.userId)
        }

        // 2) DTO -> 도메인 변환
        val product: Product = CreateProductRequest.toDomain(
            req = request,
            mainImageUrl = mainImg.url,
            subImagesUrls = subImgs.map { it.url },
            additionalImagesUrls = additionalImgs.map { it.url },
        ).copy(options = emptyList())

        validateForCreation(product)

        // 3) 상품 저장
        val savedProduct: Product = productPersistencePort.save(product)

        // 4) 옵션 등록
        saveProductOptions(savedProduct, request.options)

        // 5) 옵션까지 채워서 최종 반환
        val completeProduct: Product = enrichProduct(savedProduct)
        return ProductResponse.fromDomain(completeProduct)
    }

    /**
     * 상품 수정
     */
    @Transactional
    override fun updateProduct(
        request: UpdateProductRequest,
    ): ProductResponse {
        // 1) 기존 상품 조회
        val existingProduct: Product = productPersistencePort.findById(request.productId)
            ?: throw IllegalArgumentException("Product not found: ${request.productId}")

        // 2) 새로 넘어온 이미지들 업로드
        val newMainImg = request.mainImageFile?.let {
            uploadSingleImage(it, request.userId)
        }
        val newSubImgs = request.subImageFiles?.map {
            uploadSingleImage(it, request.userId)
        } ?: emptyList()
        val newAdditionalImgs = request.additionalImageFiles?.map {
            uploadSingleImage(it, request.userId)
        } ?: emptyList()

        // 3) DTO -> 도메인 변환
        //    (메인/서브/추가 이미지는 새 URL을 넣거나, 없으면 기존 이미지를 유지)
        val mainImageUrl = newMainImg?.url ?: existingProduct.mainImage.url
        val subImagesUrls = if (newSubImgs.isNotEmpty()) {
            newSubImgs.map { it.url }
        } else {
            existingProduct.subImages.map { it.url }
        }
        val additionalImagesUrls = if (newAdditionalImgs.isNotEmpty()) {
            newAdditionalImgs.map { it.url }
        } else {
            existingProduct.additionalImages.map { it.url }
        }

        val productFromReq = UpdateProductRequest.toDomain(
            req = request,
            mainImageUrl = mainImageUrl,
            subImagesUrls = subImagesUrls,
            additionalImagesUrls = additionalImagesUrls,
        ).copy(options = emptyList())

        productFromReq.validateForUpdate()

        // 4) 기존 엔티티로 변환 후 필드 업데이트
        val existingEntity = ProductEntity.fromDomain(existingProduct)
        updateProductEntity(existingEntity, productFromReq)

        // 이미지 교체 로직(대표, 서브, 추가)은 여기서 직접 세팅하거나
        // 기존 Embeddable List를 지우고 새로 채우는 방식으로 업데이트 가능
        if (newMainImg != null) {
            existingEntity.mainImage = ImageEmbeddable.fromDomainImage(newMainImg)
        }
        if (newSubImgs.isNotEmpty()) {
            existingEntity.subImages.clear()
            existingEntity.subImages.addAll(
                newSubImgs.map { emb ->
                    ImageEmbeddable.fromDomainImage(
                        Image(
                            userId = emb.userId,
                            fileName = emb.fileName,
                            url = emb.url
                        )
                    )
                }
            )
        }
        if (newAdditionalImgs.isNotEmpty()) {
            existingEntity.additionalImages.clear()
            existingEntity.additionalImages.addAll(
                newAdditionalImgs.map { emb ->
                    ImageEmbeddable.fromDomainImage(
                        Image(
                            userId = emb.userId,
                            fileName = emb.fileName,
                            url = emb.url
                        )
                    )
                }
            )
        }

        // 5) 옵션 처리
        val existingOptions = productOptionPersistencePort.findByProductId(existingProduct.productId)
        val updateOptionIds = request.options
            .filter { it.optionId != null && it.optionId != 0L }
            .mapNotNull { it.optionId }
            .toSet()

        //   5-1) 기존 옵션 중 요청에 없는 optionId -> 삭제
        existingOptions
            .filter { it.optionId !in updateOptionIds }
            .forEach { opt ->
                productOptionPersistencePort.deleteById(opt.optionId)
            }

        //   5-2) 신규/수정 옵션
        request.options.forEach { dto ->
            processUpdateOption(dto, existingProduct.productId)
        }

        // 6) 갱신된 엔티티 -> 도메인 저장
        val updatedDomain = productPersistencePort.save(existingEntity.toDomain())
        val completeProduct = enrichProduct(updatedDomain)
        return ProductResponse.fromDomain(completeProduct)
    }

    /**
     * 상품 삭제
     */
    @Transactional
    override fun deleteProduct(productId: Long) {
        // 1) 상품 조회
        val product: Product =
            productPersistencePort.findById(productId)
                ?: throw IllegalArgumentException("The product to delete does not exist: $productId.")

        // 2) 연결된 모든 이미지 삭제
        val allImages = listOf(product.mainImage) + product.subImages + product.additionalImages
        allImages.forEach { image ->
            imageService.delete(image.imageId)
        }

        // 3) 옵션 삭제 -> 상품 삭제
        productOptionPersistencePort.deleteAllByProductId(productId)
        productPersistencePort.deleteById(productId)
    }

    /**
     * 상품 단건 조회
     */
    override fun getProductById(productId: Long): ProductResponse {
        val product: Product = productPersistencePort.findById(productId)
            ?: throw IllegalArgumentException("Product with ID $productId not found.")
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
        size: Int
    ): PagedResponse<ProductResponse> {
        // 1) 검색
        val found = productPersistencePort.searchByCriteria(
            title = title,
            productType = productType,      // "WEDDING_SNAP" 등
            shootingPlace = shootingPlace,  // "JEJU" 등
            sortBy = sortBy,
        )

        // 2) 정렬 로직 (정말 간단한 예시)
        //    - sortBy에 따라 likes, createdDate, etc.를 기준으로 정렬할 수 있음
        val mockData = found.mapIndexed { idx, product -> Pair(product, idx + 1) }
        val sortedProducts = when (sortBy) {
            "likes" -> mockData.sortedByDescending { it.second }.map { it.first }
            "created-desc" -> found.sortedByDescending { it.createdAt }
            else -> mockData.map { it.first } // 별도 정렬 없음
        }

        // 3) 페이징
        val totalElements = sortedProducts.size.toLong()
        val totalPages = if (size > 0) ((sortedProducts.size + size - 1) / size) else 0
        val fromIndex = page * size
        val toIndex = min(fromIndex + size, sortedProducts.size)
        val pageContent =
            if (fromIndex >= sortedProducts.size) emptyList() else sortedProducts.subList(fromIndex, toIndex)

        // 4) 응답 변환
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
        // 1) 일단 전부 조회
        val all = productPersistencePort.findAll()
        // 2) 정렬 (예: 좋아요 or 최신순)
        val mockData = all.mapIndexed { idx, product -> Pair(product, idx + 1) }
        val sortedProducts = mockData.sortedByDescending { it.second }.map { it.first }

        // 3) 페이징
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

    // ===================================================
    // 아래부턴 내부 helper 메서드

    /**
     * 이미지 1장을 업로드하고 Image 도메인 객체로 반환
     */
    private fun uploadSingleImage(file: MultipartFile, userId: Long): Image {
        val cmd = SaveImageCommand(file, userId)
        val imageId = imageService.save(cmd) // 단건 업로드
        val response = imageService.getOne(imageId)
        val fileName = response.url.substringAfterLast('/')
        return Image(
            imageId = response.imageId,
            userId = userId,
            fileName = fileName,
            url = response.url,
        )
    }

    /**
     * 상품 등록 시 옵션들 저장
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
     * 상품 수정 시, 옵션 신규/수정 처리
     */
    private fun processUpdateOption(
        dto: UpdateProductOptionRequest,
        productId: Long,
    ) {
        val domainOption = UpdateProductOptionRequest.toDomain(dto, productId)
        if (domainOption.optionId != 0L) {
            // 기존 옵션 수정
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
                ?: throw IllegalArgumentException("Product not found: $productId")

            productOptionPersistencePort.save(updatedOption, product)
        } else {
            // 신규 옵션 추가
            productOptionPersistencePort.save(
                domainOption, productPersistencePort.findById(productId)
                    ?: throw IllegalArgumentException("Product not found: $productId")
            )
        }
    }

    /**
     * 기존에 저장된 상품 엔티티를, 요청 도메인을 기반으로 필드만 업데이트
     */
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

            // 다중 선택(Set) 항목들 clear 후 재설정
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

    /**
     * 상품 저장 전, 같은 유저가 동일 제목의 상품을 이미 등록했는지 확인
     */
    private fun validateForCreation(product: Product) {
        require(!productPersistencePort.existsByUserIdAndTitle(product.userId, product.title)) {
            "A product with the same title already exists: ${product.title}."
        }
    }

    /**
     * 상품에 연결된 옵션 목록 등을 추가로 불러와 완전한 도메인으로 만들기
     */
    private fun enrichProduct(product: Product): Product {
        val opts = productOptionPersistencePort.findByProductId(product.productId)
        return product.copy(options = opts)
    }
}
