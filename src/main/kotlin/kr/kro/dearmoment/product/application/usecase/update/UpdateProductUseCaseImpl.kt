package kr.kro.dearmoment.product.application.usecase.update

import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.image.application.handler.ImageHandler
import kr.kro.dearmoment.product.adapter.out.persistence.ImageEmbeddable
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
    // ProductMapper 제거: 별도의 매퍼를 사용하지 않음
    private val productOptionUseCase: ProductOptionUseCase
) : UpdateProductUseCase {

    @Transactional
    override fun updateProduct(request: UpdateProductRequest): ProductResponse {
        // 기존 상품 조회
        val existingProduct = productPersistencePort.findById(request.productId)
            ?: throw IllegalArgumentException("존재하지 않는 상품 ID: ${request.productId}")

        // 메인 이미지 교체 (새 파일이 있으면 교체, 없으면 기존 유지)
        val newMainImage: Image = request.mainImageFile?.let { file ->
            imageHandler.updateMainImage(file, request.userId, existingProduct.mainImage)
        } ?: existingProduct.mainImage

        // 서브 이미지 처리 (최종 4장)
        val updatedSubImages: List<Image> = imageHandler.processSubImagesFinal(
            currentSubImages = existingProduct.subImages,
            finalRequests = request.subImagesFinal,
            userId = request.userId
        )

        // 추가 이미지 처리 (0~5장)
        val updatedAdditionalImages: List<Image> = imageHandler.processAdditionalImagesFinal(
            currentAdditionalImages = existingProduct.additionalImages,
            finalRequests = request.additionalImagesFinal,
            userId = request.userId
        )

        // 도메인 객체 생성 (이미 Image 객체를 직접 넣어줌)
        val productFromReq = UpdateProductRequest.toDomain(
            req = request,
            mainImage = newMainImage,
            subImages = updatedSubImages,
            additionalImages = updatedAdditionalImages
        )
        productFromReq.validateForUpdate()

        // 기존 Entity를 도메인에서 생성 후 직접 업데이트
        val existingEntity = kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity.fromDomain(existingProduct)

        // 직접 필드 업데이트
        existingEntity.apply {
            userId = productFromReq.userId
            productType = productFromReq.productType
            shootingPlace = productFromReq.shootingPlace
            title = productFromReq.title
            description = productFromReq.description.takeIf { it.isNotBlank() }
            availableSeasons.clear()
            availableSeasons.addAll(productFromReq.availableSeasons)
            cameraTypes.clear()
            cameraTypes.addAll(productFromReq.cameraTypes)
            retouchStyles.clear()
            retouchStyles.addAll(productFromReq.retouchStyles)
            detailedInfo = productFromReq.detailedInfo.takeIf { it.isNotBlank() }
            contactInfo = productFromReq.contactInfo.takeIf { it.isNotBlank() }
        }

        // 서브/추가 이미지를 Embeddable로 변환 후 저장
        existingEntity.subImages = updatedSubImages.map { ImageEmbeddable.fromDomainImage(it) }.toMutableList()
        existingEntity.additionalImages =
            updatedAdditionalImages.map { ImageEmbeddable.fromDomainImage(it) }.toMutableList()

        // 옵션 동기화 (업데이트 요청 옵션에 대해 ProductOptionUseCase 내 synchronizeOptions 호출)
        productOptionUseCase.synchronizeOptions(existingProduct, request.options)

        // DB 저장 후 반환
        val updatedProduct = productPersistencePort.save(existingEntity.toDomain())
        return ProductResponse.fromDomain(updatedProduct)
    }
}
