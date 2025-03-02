package kr.kro.dearmoment.product.application.usecase.update

import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.image.application.handler.ImageHandler
import kr.kro.dearmoment.product.adapter.out.persistence.ImageEmbeddable
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.usecase.option.ProductOptionUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
    private val imageHandler: ImageHandler,
    private val productOptionUseCase: ProductOptionUseCase
) : UpdateProductUseCase {

    @Transactional
    override fun updateProduct(request: UpdateProductRequest): ProductResponse {
        val existingProduct = productPersistencePort.findById(request.productId)
            ?: throw IllegalArgumentException("존재하지 않는 상품 ID: ${request.productId}")

        // 새 mainImage 계산
        val newMainImage: Image = request.mainImageFile?.let { file ->
            imageHandler.updateMainImage(file, request.userId, existingProduct.mainImage)
        } ?: existingProduct.mainImage

        // 서브/추가 이미지 등 로직은 동일
        val updatedSubImages: List<Image> = imageHandler.processSubImagesFinal(
            currentSubImages = existingProduct.subImages,
            finalRequests = request.subImagesFinal,
            userId = request.userId
        )
        val updatedAdditionalImages: List<Image> = imageHandler.processAdditionalImagesFinal(
            currentAdditionalImages = existingProduct.additionalImages,
            finalRequests = request.additionalImagesFinal,
            userId = request.userId
        )

        val productFromReq = UpdateProductRequest.toDomain(
            req = request,
            mainImage = newMainImage,
            subImages = updatedSubImages,
            additionalImages = updatedAdditionalImages
        )
        productFromReq.validateForUpdate()

        // 기존 Entity를 도메인으로부터 생성
        val existingEntity = ProductEntity.fromDomain(existingProduct)

        // 이 부분에서 mainImage를 함께 업데이트해야 함
        existingEntity.apply {
            userId = productFromReq.userId
            productType = productFromReq.productType
            shootingPlace = productFromReq.shootingPlace
            title = productFromReq.title
            description = productFromReq.description.takeIf { it.isNotBlank() }

            // 새 mainImage 반영
            mainImage = ImageEmbeddable.fromDomainImage(productFromReq.mainImage)

            availableSeasons.clear()
            availableSeasons.addAll(productFromReq.availableSeasons)
            cameraTypes.clear()
            cameraTypes.addAll(productFromReq.cameraTypes)
            retouchStyles.clear()
            retouchStyles.addAll(productFromReq.retouchStyles)
            detailedInfo = productFromReq.detailedInfo.takeIf { it.isNotBlank() }
            contactInfo = productFromReq.contactInfo.takeIf { it.isNotBlank() }
        }

        // 서브/추가 이미지 필드도 업데이트
        existingEntity.subImages = updatedSubImages
            .map { ImageEmbeddable.fromDomainImage(it) }
            .toMutableList()
        existingEntity.additionalImages = updatedAdditionalImages
            .map { ImageEmbeddable.fromDomainImage(it) }
            .toMutableList()

        // 옵션 동기화
        productOptionUseCase.synchronizeOptions(existingProduct, request.options)

        // 최종 저장 후, 도메인 -> 응답 변환
        val updatedProduct = productPersistencePort.save(existingEntity.toDomain())
        return ProductResponse.fromDomain(updatedProduct)
    }
}