package kr.kro.dearmoment.product.application.usecase.search

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.product.application.dto.request.SearchProductRequest
import kr.kro.dearmoment.product.application.dto.response.SearchProductResponse
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
        // ① 정렬 기준 유지(오름차순)
        val pageable = PageRequest.of(page, size, Sort.by("productId").ascending())

        // ② Page<Product> 반환으로 변경된 Port 사용
        val productsPage = getProductPort.searchByCriteria(request.toQuery(), pageable)

        val productIds = productsPage.content.map { it.productId }

        val userLikes =
            userId?.let {
                getLikePort.findProductLikesByUserIdAndProductIds(it, productIds)
                    .associate { like -> like.product.productId to like.id }
            } ?: emptyMap()

        // ③ Page 객체의 메타데이터 활용하여 응답 생성
        return PagedResponse(
            content = productsPage.content.map { SearchProductResponse.from(it, userLikes) },
            page = productsPage.number,
            size = productsPage.size
        )
    }
}
