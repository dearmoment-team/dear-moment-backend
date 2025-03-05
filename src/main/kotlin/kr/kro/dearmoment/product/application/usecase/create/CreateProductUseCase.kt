package kr.kro.dearmoment.product.application.usecase.create

import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse

interface CreateProductUseCase {
    fun saveProduct(request: CreateProductRequest): ProductResponse
}
