package kr.kro.dearmoment.product.application.usecase.get

import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class GetProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
) : GetProductUseCase {
    override fun getProductById(productId: Long): ProductResponse {
        val product =
            productPersistencePort.findById(productId)
                ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)
        return ProductResponse.fromDomain(product)
    }
}
