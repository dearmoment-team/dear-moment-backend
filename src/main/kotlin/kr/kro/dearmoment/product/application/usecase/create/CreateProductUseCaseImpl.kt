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
import kr.kro.dearmoment.user.application.port.output.GetStudioUserPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class CreateProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
    private val getProductPort: GetProductPort,
    private val imageService: ImageService,
    private val getStudioUserPort: GetStudioUserPort,
) : CreateProductUseCase {
    @Transactional
    override fun saveProduct(
        request: CreateProductRequest,
        userId: UUID,
        mainImageFile: MultipartFile,
        subImageFiles: List<MultipartFile>,
        additionalImageFiles: List<MultipartFile>,
    ): ProductResponse {
        // 스튜디오 권한 확인: 스튜디오 소유자가 아닌 경우 예외 발생
        getStudioUserPort.findStudioUserById(userId)

        // 서브 이미지 검증: 정확히 4장이어야 함
        if (subImageFiles.size != 4) {
            throw CustomException(ErrorCode.INVALID_SUB_IMAGE_COUNT)
        }
        // 추가 이미지 검증: 최대 5장까지만 가능
        if (additionalImageFiles.size > 5) {
            throw CustomException(ErrorCode.INVALID_ADDITIONAL_IMAGE_COUNT)
        }

        // 이미지 업로드
        val mainImg = imageService.save(SaveImageCommand(mainImageFile, userId))
        val subImgs = subImageFiles.map { imageService.save(SaveImageCommand(it, userId)) }
        val additionalImgs = additionalImageFiles.map { imageService.save(SaveImageCommand(it, userId)) }

        // 도메인 객체 생성 (옵션은 DTO 변환에서 이미 포함됨)
        val product: Product =
            CreateProductRequest.toDomain(
                req = request,
                userId = userId,
                mainImage = mainImg,
                subImages = subImgs,
                additionalImages = additionalImgs,
            )

        // 생성 전 유효성 검사: 동일 제목의 상품이 이미 존재하는지 확인
        validateForCreation(product)
        val savedProduct = productPersistencePort.save(product, request.studioId)

        // 저장된 상품 조회 실패 시 예외 처리
        val completeProduct: Product =
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
