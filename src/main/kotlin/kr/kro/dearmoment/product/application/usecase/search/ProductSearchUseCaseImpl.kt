package kr.kro.dearmoment.product.application.usecase.search

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.usecase.util.PaginationUtil
import org.springframework.stereotype.Service

@Service
class ProductSearchUseCaseImpl(
    private val getProductPort: GetProductPort,
    private val paginationUtil: PaginationUtil,
) : ProductSearchUseCase {
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

    override fun getMainPageProducts(
        page: Int,
        size: Int,
    ): PagedResponse<ProductResponse> {
        val all = getProductPort.findAll()
        val mockData = all.mapIndexed { idx, product -> Pair(product, idx + 1) }
        val sortedProducts = mockData.sortedByDescending { it.second }.map { it.first }
        return paginationUtil.createPagedResponse(sortedProducts, page, size)
    }
}
