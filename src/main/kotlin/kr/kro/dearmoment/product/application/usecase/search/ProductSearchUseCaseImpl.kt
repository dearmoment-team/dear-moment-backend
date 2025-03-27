package kr.kro.dearmoment.product.application.usecase.search

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.product.application.dto.request.SearchProductRequest
import kr.kro.dearmoment.product.application.dto.response.SearchProductResponse
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductSearchUseCaseImpl(
    private val getProductPort: GetProductPort,
) : ProductSearchUseCase {
    @Transactional(readOnly = true)
    override fun searchProducts(
        request: SearchProductRequest,
        page: Int,
        size: Int,
    ): PagedResponse<SearchProductResponse> {
        val pageable = PageRequest.of(page, size)
        val products = getProductPort.searchByCriteria(request.toQuery(), pageable)

        return PagedResponse(
            content = products.content.map { SearchProductResponse.from(it) },
            page = page,
            size = size,
            totalElements = products.totalElements,
            totalPages = products.totalPages,
        )
    }
}
