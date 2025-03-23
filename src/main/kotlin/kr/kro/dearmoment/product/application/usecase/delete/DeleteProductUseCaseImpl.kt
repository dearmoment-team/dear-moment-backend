package kr.kro.dearmoment.product.application.usecase.delete

import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
    private val imageService: ImageService,
) : DeleteProductUseCase {
    @Transactional
    override fun deleteProduct(productId: Long) {
        val product =
            productPersistencePort.findById(productId)
                ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)

        // 이미지 삭제
        (listOf(product.mainImage) + product.subImages + product.additionalImages).forEach {
            imageService.delete(it.imageId)
        }

        productPersistencePort.deleteById(productId)
    }
}
