package kr.kro.dearmoment.product.application.dto.query

import kr.kro.dearmoment.product.domain.sort.SortCriteria

data class SearchProductQuery(
    val sortBy: SortCriteria,
    val availableSeasons: MutableSet<String>,
    val cameraTypes: MutableSet<String>,
    val retouchStyles: MutableSet<String>,
    val partnerShopCategories: MutableSet<String>,
    val minPrice: Long,
    val maxPrice: Long,
)
