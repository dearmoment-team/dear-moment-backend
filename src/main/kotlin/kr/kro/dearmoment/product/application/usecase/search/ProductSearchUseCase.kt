package kr.kro.dearmoment.product.application.usecase.search

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.product.application.dto.request.SearchProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.dto.response.SearchProductResponse

interface ProductSearchUseCase {
    fun searchProducts(
        title: String?,
        productType: String?,
        shootingPlace: String?,
        sortBy: String?,
        page: Int,
        size: Int,
    ): PagedResponse<ProductResponse>

    fun getMainPageProducts(
        page: Int,
        size: Int,
    ): PagedResponse<ProductResponse>

    fun searchProducts2(
        request: SearchProductRequest,
        page: Int,
        size: Int,
    ): PagedResponse<SearchProductResponse>
}
