package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse

interface ProductUseCase {
    fun saveProduct(request: CreateProductRequest): ProductResponse

    fun updateProduct(request: UpdateProductRequest): ProductResponse

    fun deleteProduct(productId: Long)

    fun getProductById(productId: Long): ProductResponse

    fun searchProducts(
        title: String?,
        minPrice: Long?,
        maxPrice: Long?,
        typeCode: Int? = null,
        sortBy: String? = null,
        page: Int = 0,
        size: Int = 10,
    ): PagedResponse<ProductResponse>

    fun getMainPageProducts(
        page: Int = 0,
        size: Int = 10,
    ): PagedResponse<ProductResponse>
}
