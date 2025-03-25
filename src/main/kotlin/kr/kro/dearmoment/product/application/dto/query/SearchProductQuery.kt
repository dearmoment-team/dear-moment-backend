package kr.kro.dearmoment.product.application.dto.query

import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.product.domain.sort.SortCriteria

data class SearchProductQuery(
    val sortBy: SortCriteria,
    val availableSeasons: List<ShootingSeason>,
    val cameraTypes: List<CameraType>,
    val retouchStyles: List<RetouchStyle>,
    val partnerShopCategories: List<PartnerShopCategory>,
    val minPrice: Long,
    val maxPrice: Long,
)
