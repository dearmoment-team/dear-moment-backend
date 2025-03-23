package kr.kro.dearmoment.product.application.usecase.update

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.application.handler.ImageHandler
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.adapter.out.persistence.ImageEmbeddable
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
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
    /**
     * 업데이트 요청 시 파일 파라미터(대표, 서브, 추가 이미지 파일)는 컨트롤러에서 별도로 전달받습니다.
     * 옵션(request)은 선택사항입니다.
     *
     * 이미지 업로드 로직 변경 사항(CreateProductRequest의 toDomain 참고)을 반영하여,
     * 새롭게 매핑된 이미지 객체를 생성한 후 DTO -> 도메인 변환을 수행합니다.
     */
    @Transactional
    override fun updateProduct(
        productId: Long,
        rawRequest: UpdateProductRequest,
        mainImageFile: MultipartFile?,
        subImageFiles: List<MultipartFile>?,
        additionalImageFiles: List<MultipartFile>?,
        options: List<UpdateProductOptionRequest>?,
    ): ProductResponse {
        // 1) DB에서 기존 Product 조회
        val existingProduct =
            productPersistencePort.findById(productId)
                ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)

        // 2) 대표 이미지 처리: 새 파일이 전달되면 업로드 후 업데이트, 없으면 기존 이미지 유지
        val newMainImage: Image =
            mainImageFile?.let { file ->
                imageHandler.updateMainImage(file, rawRequest.userId, existingProduct.mainImage)
            } ?: existingProduct.mainImage

        // 3) 서브 이미지 처리: 부분 업데이트를 지원
        val updatedSubImages =
            rawRequest.subImagesFinal?.let { finalRequests ->
                imageHandler.processSubImagesPartial(
                    currentSubImages = existingProduct.subImages,
                    finalRequests = finalRequests,
                    subImageFiles = subImageFiles ?: emptyList(),
                    userId = rawRequest.userId,
                )
            } ?: existingProduct.subImages

        // 4) 추가 이미지 처리: 파일과 메타데이터를 함께 처리 (메타데이터는 DTO의 additionalImagesFinal에서 전달)
        val updatedAdditionalImages =
            imageHandler.processAdditionalImagesFinal(
                currentAdditionalImages = existingProduct.additionalImages,
                finalRequests = rawRequest.additionalImagesFinal ?: emptyList(),
                additionalImageFiles = additionalImageFiles,
                userId = rawRequest.userId,
            )

        // 새 이미지 업로드 로직 (CreateProductRequest와 동일한 매핑 로직)
        val mappedMainImage =
            Image(
                userId = rawRequest.userId,
                imageId = newMainImage.imageId,
                fileName = newMainImage.fileName,
                parId = newMainImage.parId,
                url = newMainImage.url,
            )
        val mappedSubImages =
            updatedSubImages.map { image ->
                Image(
                    userId = rawRequest.userId,
                    imageId = image.imageId,
                    fileName = image.fileName,
                    parId = image.parId,
                    url = image.url,
                )
            }
        val mappedAdditionalImages =
            updatedAdditionalImages.map { image ->
                Image(
                    userId = rawRequest.userId,
                    imageId = image.imageId,
                    fileName = image.fileName,
                    parId = image.parId,
                    url = image.url,
                )
            }

        // 5) DTO -> 도메인 객체 변환 (기존 Product와 병합)
        val productFromReq =
            UpdateProductRequest.toDomain(
                req = rawRequest,
                existingProduct = existingProduct,
                mainImage = mappedMainImage,
                subImages = mappedSubImages,
                additionalImages = mappedAdditionalImages,
                options = options ?: emptyList(),
            )
        productFromReq.validateForUpdate()

        // 6) 기존 Entity의 필드 업데이트
        val existingEntity =
            ProductEntity.fromDomain(existingProduct).apply {
                // 병합된 결과로 업데이트 (필요에 따라 null 처리된 필드는 기존 값 유지)
                userId = productFromReq.userId
                productType = productFromReq.productType
                shootingPlace = productFromReq.shootingPlace
                title = productFromReq.title
                description = productFromReq.description.takeIf { it.isNotBlank() } ?: ""
                mainImage = ImageEmbeddable.fromDomainImage(productFromReq.mainImage)

                availableSeasons.clear()
                availableSeasons.addAll(productFromReq.availableSeasons)
                cameraTypes.clear()
                cameraTypes.addAll(productFromReq.cameraTypes)
                retouchStyles.clear()
                retouchStyles.addAll(productFromReq.retouchStyles)
                detailedInfo = productFromReq.detailedInfo.takeIf { it.isNotBlank() } ?: ""
                contactInfo = productFromReq.contactInfo.takeIf { it.isNotBlank() } ?: ""

                // 서브 및 추가 이미지 업데이트 (새롭게 매핑된 이미지 객체 사용)
                subImages = mappedSubImages.map { ImageEmbeddable.fromDomainImage(it) }.toMutableList()
                additionalImages = mappedAdditionalImages.map { ImageEmbeddable.fromDomainImage(it) }.toMutableList()
            }

        // 7) 옵션 동기화: 옵션 요청이 있을 경우에만 업데이트 및 신규 옵션 추가 처리 (삭제는 별도 API 포인트에서 처리)
        options?.let {
            productOptionUseCase.synchronizeOptions(existingProduct, it)
        }

        // 8) 최종 저장 및 응답 객체 변환
        val updatedProduct = productPersistencePort.save(existingEntity.toDomain())
        return ProductResponse.fromDomain(updatedProduct)
    }
}
