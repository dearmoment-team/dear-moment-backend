package kr.kro.dearmoment.product.application.usecase.delete

import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
) : DeleteProductUseCase {
    @Transactional
    override fun deleteProduct(productId: Long) {
        val product =
            productPersistencePort.findById(productId)
                ?: throw IllegalArgumentException("삭제할 상품이 존재하지 않습니다. ID: $productId")

        productPersistencePort.deleteById(productId)
    }
}
