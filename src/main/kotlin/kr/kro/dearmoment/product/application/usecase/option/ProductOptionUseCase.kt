package kr.kro.dearmoment.product.application.usecase.option

import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.domain.model.Product

interface ProductOptionUseCase {
    fun saveProductOption(
        productId: Long,
        request: CreateProductOptionRequest,
    ): ProductOptionResponse

    fun getProductOptionById(optionId: Long): ProductOptionResponse

    fun getAllProductOptions(): List<ProductOptionResponse>

    fun deleteProductOptionById(optionId: Long)

    fun getProductOptionsByProductId(productId: Long): List<ProductOptionResponse>

    fun deleteAllProductOptionsByProductId(productId: Long)

    fun existsProductOptions(productId: Long): Boolean

    fun synchronizeOptions(
        existingProduct: Product,
        requestOptions: List<UpdateProductOptionRequest>,
    )

    fun saveOrUpdateProductOption(
        productId: Long,
        request: UpdateProductOptionRequest?,
    ): ProductOptionResponse?
}
