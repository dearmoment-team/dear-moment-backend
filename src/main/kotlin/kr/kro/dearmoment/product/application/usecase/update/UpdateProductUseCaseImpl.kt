package kr.kro.dearmoment.product.application.usecase.update

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.application.handler.ImageHandler
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.adapter.out.persistence.ImageEmbeddable
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.usecase.option.ProductOptionUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class UpdateProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
    private val imageHandler: ImageHandler,
    private val productOptionUseCase: ProductOptionUseCase,
) : UpdateProductUseCase {
    @Transactional
    override fun updateProduct(
        productId: Long,
        rawRequest: UpdateProductRequest,
        mainImageFile: MultipartFile?,
        subImageFiles: List<MultipartFile>?,
        additionalImageFiles: List<MultipartFile>?,
    ): ProductResponse {
        // 1) DB에서 기존 Product 조회
        val existingProduct =
            productPersistencePort.findById(productId)
                ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)

        // 2) 이미지 핸들러를 이용해, rawRequest + 파일 목록들을 최종 UpdateProductRequest로 merge
        val mergedRequest =
            imageHandler.mergeUpdateRequest(
                productId = productId,
                rawRequest = rawRequest,
                mainImageFile = mainImageFile,
                subImageFiles = subImageFiles,
                additionalImageFiles = additionalImageFiles,
            )

        // 3) 대표 이미지 교체 (있다면)
        val newMainImage: Image =
            mergedRequest.mainImageFile?.let { file ->
                imageHandler.updateMainImage(file, mergedRequest.userId, existingProduct.mainImage)
            } ?: existingProduct.mainImage

        // 4) 서브/추가 이미지 최종 처리
        val updatedSubImages =
            imageHandler.processSubImagesFinal(
                currentSubImages = existingProduct.subImages,
                finalRequests = mergedRequest.subImagesFinal,
                userId = mergedRequest.userId,
            )
        val updatedAdditionalImages =
            imageHandler.processAdditionalImagesFinal(
                currentAdditionalImages = existingProduct.additionalImages,
                finalRequests = mergedRequest.additionalImagesFinal,
                userId = mergedRequest.userId,
            )

        // 5) DTO -> 도메인
        val productFromReq =
            UpdateProductRequest.toDomain(
                req = mergedRequest,
                mainImage = newMainImage,
                subImages = updatedSubImages,
                additionalImages = updatedAdditionalImages,
            )
        productFromReq.validateForUpdate()

        // 6) 기존 Entity 기반으로 필드 업데이트
        val existingEntity =
            ProductEntity.fromDomain(existingProduct).apply {
                userId = productFromReq.userId
                productType = productFromReq.productType
                shootingPlace = productFromReq.shootingPlace
                title = productFromReq.title
                description = productFromReq.description.takeIf { it.isNotBlank() }

                mainImage = ImageEmbeddable.fromDomainImage(productFromReq.mainImage)

                availableSeasons.clear()
                availableSeasons.addAll(productFromReq.availableSeasons)
                cameraTypes.clear()
                cameraTypes.addAll(productFromReq.cameraTypes)
                retouchStyles.clear()
                retouchStyles.addAll(productFromReq.retouchStyles)
                detailedInfo = productFromReq.detailedInfo.takeIf { it.isNotBlank() }
                contactInfo = productFromReq.contactInfo.takeIf { it.isNotBlank() }

                // 서브/추가 이미지 교체
                subImages = updatedSubImages.map { ImageEmbeddable.fromDomainImage(it) }.toMutableList()
                additionalImages = updatedAdditionalImages.map { ImageEmbeddable.fromDomainImage(it) }.toMutableList()
            }

        // 7) 옵션 동기화
        productOptionUseCase.synchronizeOptions(existingProduct, mergedRequest.options)

        // 8) 최종 저장 & 응답
        val updatedProduct = productPersistencePort.save(existingEntity.toDomain())
        return ProductResponse.fromDomain(updatedProduct)
    }
}
