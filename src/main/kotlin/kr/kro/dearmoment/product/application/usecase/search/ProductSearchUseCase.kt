package kr.kro.dearmoment.product.application.usecase.search

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse

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
}
