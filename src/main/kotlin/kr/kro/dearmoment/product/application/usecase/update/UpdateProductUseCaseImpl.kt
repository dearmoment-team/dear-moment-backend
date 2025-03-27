package kr.kro.dearmoment.product.application.usecase.update

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.application.handler.ImageHandler
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.image.domain.withUserId
import kr.kro.dearmoment.product.adapter.out.persistence.ImageEmbeddable
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.usecase.option.ProductOptionUseCase
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity
import kr.kro.dearmoment.studio.application.port.output.GetStudioPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class UpdateProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
    private val getProductPort: GetProductPort,
    private val imageHandler: ImageHandler,
    private val productOptionUseCase: ProductOptionUseCase,
    private val getStudioPort: GetStudioPort,
) : UpdateProductUseCase {
    /**
     * 업데이트 요청 시 파일 파라미터(대표, 서브, 추가 이미지 파일)는 컨트롤러에서 별도로 전달받습니다.
     * 옵션(request)은 선택사항입니다.
     *
     * 이미지 업로드 로직 변경 사항(CreateProductRequest의 toDomain 참고)을 반영하여,
     * 새롭게 매핑된 이미지 객체를 생성한 후 DTO -> 도메인 변환을 수행합니다.
     *
     * **소유권 검증:** 인증된 userId(파라미터)와 DB에서 조회한 기존 상품의 소유자(existingProduct.userId)가
     * 일치하지 않으면 업데이트를 허용하지 않습니다.
     */
    @Transactional
    override fun updateProduct(
        userId: UUID,
        productId: Long,
        rawRequest: UpdateProductRequest,
        mainImageFile: MultipartFile?,
        subImageFiles: List<MultipartFile>?,
        additionalImageFiles: List<MultipartFile>?,
        options: List<UpdateProductOptionRequest>?,
    ): ProductResponse {
        // 1) DB에서 기존 Product 조회
        val existingProduct =
            getProductPort.findById(productId)
                ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)

        // 1-1) 소유권 검증: 인증된 userId와 기존 상품의 소유자(userId)가 일치하는지 확인
        if (existingProduct.userId != userId) {
            throw CustomException(ErrorCode.UNAUTHORIZED_ACCESS)
        }

        // 2) 대표 이미지 처리: 새 파일이 전달되면 업로드 후 업데이트, 없으면 기존 이미지 유지
        val newMainImage: Image =
            mainImageFile?.let { file ->
                imageHandler.updateMainImage(file, userId, existingProduct.mainImage)
            } ?: existingProduct.mainImage

        // 3) 서브 이미지 처리: 부분 업데이트를 지원
        val updatedSubImages =
            rawRequest.subImagesFinal?.let { finalRequests ->
                imageHandler.processSubImagesPartial(
                    currentSubImages = existingProduct.subImages,
                    finalRequests = finalRequests,
                    subImageFiles = subImageFiles ?: emptyList(),
                    userId = userId,
                )
            } ?: existingProduct.subImages

        // 4) 추가 이미지 처리: 파일과 메타데이터를 함께 처리 (메타데이터는 DTO의 additionalImagesFinal에서 전달)
        val updatedAdditionalImages =
            imageHandler.processAdditionalImagesFinal(
                currentAdditionalImages = existingProduct.additionalImages,
                finalRequests = rawRequest.additionalImagesFinal ?: emptyList(),
                additionalImageFiles = additionalImageFiles,
                userId = userId,
            )

        // 새 이미지 업로드 로직: 확장 함수 withUserId를 활용하여 userId 재설정
        val mappedMainImage = newMainImage.withUserId(userId)
        val mappedSubImages = updatedSubImages.map { it.withUserId(userId) }
        val mappedAdditionalImages = updatedAdditionalImages.map { it.withUserId(userId) }

        // 5) DTO -> 도메인 객체 변환 (기존 Product와 병합)
        val productFromReq =
            UpdateProductRequest.toDomain(
                req = rawRequest,
                existingProduct = existingProduct,
                mainImage = mappedMainImage,
                subImages = mappedSubImages,
                additionalImages = mappedAdditionalImages,
                options = options ?: emptyList(),
                userId = userId,
            )
        productFromReq.validateForUpdate()

        // 6) 스튜디오 엔티티 조회 및 변환
        val studio = StudioEntity.from(getStudioPort.findById(rawRequest.studioId))

        // 7) 기존 Entity의 필드 업데이트 (userId 재할당은 제거합니다)
        val existingEntity =
            ProductEntity.fromDomain(existingProduct, studio).apply {
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

        // 8) 옵션 동기화: 옵션 요청이 있을 경우에만 업데이트 및 신규 옵션 추가 처리 (삭제는 별도 API 포인트에서 처리)
        options?.let {
            productOptionUseCase.synchronizeOptions(existingProduct, it)
        }

        // 9) 최종 저장 및 응답 객체 변환
        val updatedProduct = productPersistencePort.save(existingEntity.toDomain(), studio.id)
        return ProductResponse.fromDomain(updatedProduct)
    }
}
