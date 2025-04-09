package kr.kro.dearmoment.product.adapter.out.persistence.dto

data class SearchProductOrderByPriceDto(
    val productId: Long,
    val boundaryPrice: Long,
)
