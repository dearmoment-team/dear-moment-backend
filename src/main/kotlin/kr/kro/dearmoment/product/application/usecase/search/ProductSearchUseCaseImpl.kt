package kr.kro.dearmoment.product.application.usecase.search

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.product.application.dto.request.SearchProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.dto.response.SearchProductResponse
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.usecase.util.PaginationUtil
import kr.kro.dearmoment.product.domain.sort.ProductSortCriteria
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductSearchUseCaseImpl(
    private val getProductPort: GetProductPort,
    private val paginationUtil: PaginationUtil,
) : ProductSearchUseCase {
    @Transactional(readOnly = true)
    override fun searchProducts(
        title: String?,
        productType: String?,
        shootingPlace: String?,
        sortBy: String?,
        page: Int,
        size: Int,
    ): PagedResponse<ProductResponse> {
        val found = getProductPort.searchByCriteria(title, productType, shootingPlace, sortBy)
        val sorted =
            when (sortBy) {
                "created-desc" -> found.sortedByDescending { it.productId }
                else -> found
            }
        return paginationUtil.createPagedResponse(sorted, page, size)
    }

    @Transactional(readOnly = true)
    override fun getMainPageProducts(
        page: Int,
        size: Int,
    ): PagedResponse<ProductResponse> {
        val all = getProductPort.findAll()
        val mockData = all.mapIndexed { idx, product -> Pair(product, idx + 1) }
        val sortedProducts = mockData.sortedByDescending { it.second }.map { it.first }
        return paginationUtil.createPagedResponse(sortedProducts, page, size)
    }

    @Transactional(readOnly = true)
    override fun searchProducts2(
        request: SearchProductRequest,
        page: Int,
        size: Int,
    ): PagedResponse<SearchProductResponse> {
        val pageable = PageRequest.of(page, size)
        val products = getProductPort.searchByCriteria2(request, pageable)
        val criteria = ProductSortCriteria.from(request.sortBy)
        val sortedProducts = criteria.strategy.sort(products.content)

        return PagedResponse(
            content = sortedProducts.map { SearchProductResponse.from(it) },
            page = page,
            size = size,
            totalElements = products.totalElements,
            totalPages = products.totalPages,
        )
    }
}
