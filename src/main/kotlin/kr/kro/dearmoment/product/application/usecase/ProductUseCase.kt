package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse

interface ProductUseCase {
    fun saveProduct(request: CreateProductRequest): ProductResponse

    fun updateProduct(request: UpdateProductRequest): ProductResponse

    fun deleteProduct(productId: Long)

    fun getProductById(productId: Long): ProductResponse

    fun getMainPageProducts(
        page: Int,
        size: Int,
    ): PagedResponse<ProductResponse>
}
