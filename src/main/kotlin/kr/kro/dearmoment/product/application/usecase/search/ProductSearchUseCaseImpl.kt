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
        val productIds = products.content.map { it.productId }
        val userLikes =
            userId?.let {
                getLikePort.findUserProductLikesWithoutPage(userId, productIds)
                    .map { it.product.productId }.toSet()
            } ?: emptySet()

        return PagedResponse(
            content = products.content.map { SearchProductResponse.from(it, userLikes) },
            page = page,
            size = size,
            totalElements = products.totalElements,
            totalPages = products.totalPages,
        )
    }
}
