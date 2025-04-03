package kr.kro.dearmoment.product.application.usecase.get

import kr.kro.dearmoment.product.application.dto.response.GetProductResponse
import java.util.UUID

interface GetProductUseCase {
    fun getProductById(
        productId: Long,
        userId: UUID?,
    ): GetProductResponse

    fun getMyProduct(
        userId: UUID
    ): GetProductResponse
}
