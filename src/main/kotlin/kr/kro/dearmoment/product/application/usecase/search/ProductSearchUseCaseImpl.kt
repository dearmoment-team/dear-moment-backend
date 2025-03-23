package kr.kro.dearmoment.product.application.usecase.search

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.usecase.util.PaginationUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductSearchUseCaseImpl(
    private val productPersistencePort: ProductPersistencePort,
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
        val found = productPersistencePort.searchByCriteria(title, productType, shootingPlace, sortBy)
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
        val all = productPersistencePort.findAll()
        val mockData = all.mapIndexed { idx, product -> Pair(product, idx + 1) }
        val sortedProducts = mockData.sortedByDescending { it.second }.map { it.first }
        return paginationUtil.createPagedResponse(sortedProducts, page, size)
    }
}
