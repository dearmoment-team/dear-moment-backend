package kr.kro.dearmoment.product.application.usecase.search

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.product.application.dto.request.SearchProductRequest
import kr.kro.dearmoment.product.application.dto.response.SearchProductResponse
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProductSearchUseCaseImpl(
    private val getProductPort: GetProductPort,
    private val getLikePort: GetLikePort,
) : ProductSearchUseCase {
    @Transactional(readOnly = true)
    override fun searchProducts(
        userId: UUID?,
        request: SearchProductRequest,
        page: Int,
        size: Int,
    ): PagedResponse<SearchProductResponse> {
        val pageable = PageRequest.of(page, size)
        val products = getProductPort.searchByCriteria(request.toQuery(), pageable)
        val productIds = products.map { it.productId }
        val userLikes =
            userId?.let {
                getLikePort.findProductLikesByUserIdAndProductIds(userId, productIds)
                    .associate { it.product.productId to it.id }
            } ?: emptyMap()

        return PagedResponse(
            content = products.map { SearchProductResponse.from(it, userLikes) },
            page = page,
            size = size,
        )
    }
}
