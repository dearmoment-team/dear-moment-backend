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

    /**
     * 상품 검색:
     * 기존의 가격 조건(minPrice, maxPrice)와 typeCode 대신 productType, shootingPlace, sortBy 등을 기준으로 검색합니다.
     */
    fun searchProducts(
        title: String?,
        productType: String?,  // 예: "WEDDING_SNAP"
        shootingPlace: String?, // 예: "JEJU"
        sortBy: String?,
        page: Int = 0,
        size: Int = 10,
    ): PagedResponse<ProductResponse>

    fun getMainPageProducts(
        page: Int = 0,
        size: Int = 10,
    ): PagedResponse<ProductResponse>
}
