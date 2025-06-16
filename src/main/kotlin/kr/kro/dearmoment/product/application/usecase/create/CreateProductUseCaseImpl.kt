package kr.kro.dearmoment.product.application.usecase.create

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.studio.application.port.output.GetStudioPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class CreateProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
    private val getProductPort: GetProductPort,
    private val imageService: ImageService,
    private val getStudioPort: GetStudioPort,
) : CreateProductUseCase {
    @Transactional
    override fun saveProduct(
        request: CreateProductRequest,
        userId: UUID,
        mainImageFile: MultipartFile,
        subImageFiles: List<MultipartFile>,
        additionalImageFiles: List<MultipartFile>,
    ): ProductResponse {
        // 1) 스튜디오 조회 & 소유자 검증
        val studio = getStudioPort.findById(request.studioId)
        if (studio.userId != userId) {
            throw CustomException(ErrorCode.UNAUTHORIZED_ACCESS)
        }

        // 2) 이미지 개수 검증
        if (subImageFiles.size != 4) {
            throw CustomException(ErrorCode.INVALID_SUB_IMAGE_COUNT)
        }
        if (additionalImageFiles.size > 5) {
            throw CustomException(ErrorCode.INVALID_ADDITIONAL_IMAGE_COUNT)
        }

        // 3) 이미지 저장
        val totalImages =
            buildList {
                add(mainImageFile)
                addAll(subImageFiles)
                addAll(additionalImageFiles)
            }.map { file -> SaveImageCommand(file, userId) }

        val savedImages = imageService.saveAll(totalImages)

        // 4) 저장 결과 분할
        val subImageSize = subImageFiles.size
        val additionalImageSize = additionalImageFiles.size

        val mainImg = savedImages.first()

        val subImgs =
            if (subImageSize > 0) {
                savedImages.subList(1, 1 + subImageSize)
            } else {
                emptyList()
            }

        val additionalImgs =
            if (additionalImageSize > 0) {
                savedImages.subList(
                    1 + subImageSize,
                    1 + subImageSize + additionalImageSize
                )
            } else {
                emptyList()
            }

        // 5) 도메인 객체 생성
        val product =
            CreateProductRequest.toDomain(
                req = request,
                userId = userId,
                mainImage = mainImg,
                subImages = subImgs,
                additionalImages = additionalImgs,
            )

        // 6) 동일 제목 중복 검사
        validateForCreation(product)

        // 7) 저장 및 최종 조회
        val savedProduct = productPersistencePort.save(product, request.studioId)
        val completeProduct =
            getProductPort.findById(savedProduct.productId)
                ?: throw CustomException(ErrorCode.SAVED_PRODUCT_NOT_FOUND)

        return ProductResponse.fromDomain(completeProduct)
    }

    private fun validateForCreation(product: Product) {
        if (getProductPort.existsByUserIdAndTitle(product.userId, product.title)) {
            throw CustomException(ErrorCode.PRODUCT_ALREADY_EXISTS)
        }
    }
}
