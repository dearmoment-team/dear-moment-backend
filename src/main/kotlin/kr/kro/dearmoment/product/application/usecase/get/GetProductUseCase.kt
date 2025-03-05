package kr.kro.dearmoment.product.application.usecase.get

import kr.kro.dearmoment.product.application.dto.response.ProductResponse

interface GetProductUseCase {
    fun getProductById(productId: Long): ProductResponse
}
