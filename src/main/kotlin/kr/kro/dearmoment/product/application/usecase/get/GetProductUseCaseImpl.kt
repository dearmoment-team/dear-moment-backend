package kr.kro.dearmoment.product.application.usecase.get

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetProductUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
) : GetProductUseCase {
    @Transactional(readOnly = true)
    override fun getProductById(productId: Long): ProductResponse {
        val product =
            productPersistencePort.findById(productId)
                ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)
        return ProductResponse.fromDomain(product)
    }
}
