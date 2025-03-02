package kr.kro.dearmoment.product.application.usecase.get

import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import org.springframework.stereotype.Service

@Service
class GetProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
) : GetProductUseCase {
    override fun getProductById(productId: Long): ProductResponse {
        val product =
            productPersistencePort.findById(productId)
                ?: throw IllegalArgumentException("상품을 찾을 수 없습니다. ID: $productId")
        return ProductResponse.fromDomain(product)
    }
}
