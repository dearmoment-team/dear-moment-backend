package kr.kro.dearmoment.product.application.usecase.util

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Component
import kotlin.math.ceil
import kotlin.math.min

@Component
class PaginationUtil {
    fun createPagedResponse(
        sorted: List<Product>,
        page: Int,
        size: Int,
    ): PagedResponse<ProductResponse> {
        val totalElements = sorted.size.toLong()
        val totalPages = ceil(totalElements / size.toDouble()).toInt()
        val fromIndex = page * size
        val toIndex = min(fromIndex + size, sorted.size)
        val pageContent = if (fromIndex >= sorted.size) emptyList() else sorted.subList(fromIndex, toIndex)
        return PagedResponse(
            content = pageContent.map { ProductResponse.fromDomain(it) },
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }
}
