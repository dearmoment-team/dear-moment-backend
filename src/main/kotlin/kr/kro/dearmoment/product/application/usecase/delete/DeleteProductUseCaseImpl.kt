package kr.kro.dearmoment.product.application.usecase.delete

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
    private val getProductPort: GetProductPort,
    private val imageService: ImageService,
) : DeleteProductUseCase {
    /**
     * 인증된 userId와 DB에서 조회한 기존 상품의 소유자가 일치하지 않으면 예외를 발생시킵니다.
     * 일치하는 경우, 상품에 속한 모든 이미지 삭제 후 상품을 삭제합니다.
     */
    @Transactional
    override fun deleteProduct(
        userId: UUID,
        productId: Long,
    ) {
        val product =
            getProductPort.findById(productId)
                ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)

        if (product.userId != userId) {
            throw CustomException(ErrorCode.UNAUTHORIZED_ACCESS)
        }

        // 이미지 삭제
        (listOf(product.mainImage) + product.subImages + product.additionalImages).forEach {
            imageService.delete(it.imageId)
        }

        productPersistencePort.deleteById(productId)
    }
}
