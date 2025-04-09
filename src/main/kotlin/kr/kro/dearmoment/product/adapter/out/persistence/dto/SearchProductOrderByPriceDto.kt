package kr.kro.dearmoment.product.adapter.out.persistence.dto

import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity

data class SearchProductOrderByPriceDto(
    val productEntity: ProductEntity,
    val boundaryPrice: Long,
)
