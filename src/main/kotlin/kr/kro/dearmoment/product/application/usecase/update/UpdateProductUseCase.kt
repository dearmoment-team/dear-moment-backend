package kr.kro.dearmoment.product.application.usecase.update

import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse

interface UpdateProductUseCase {
    fun updateProduct(request: UpdateProductRequest): ProductResponse
}
