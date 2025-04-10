package kr.kro.dearmoment.like.application.query

import kr.kro.dearmoment.product.adapter.out.persistence.sort.SortCriteria
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory

data class FilterUserLikesQuery(
    val sortBy: SortCriteria,
    val availableSeasons: Set<ShootingSeason>,
    val cameraTypes: Set<CameraType>,
    val retouchStyles: Set<RetouchStyle>,
    val partnerShopCategories: Set<PartnerShopCategory>,
    val minPrice: Long,
    val maxPrice: Long,
)
