package kr.kro.dearmoment.product.application.usecase.search

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.product.application.dto.request.SearchProductRequest
import kr.kro.dearmoment.product.application.dto.response.SearchProductResponse
import java.util.UUID

interface ProductSearchUseCase {
    fun searchProducts(
        userId: UUID?,
        request: SearchProductRequest,
        page: Int,
        size: Int,
    ): PagedResponse<SearchProductResponse>
}
