package kr.kro.dearmoment.like.application.dto

import kr.kro.dearmoment.common.validation.EnumValue
import kr.kro.dearmoment.like.application.query.FilterUserLikesQuery
import kr.kro.dearmoment.like.domain.SortCriteria
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory

data class FilterUserLikesRequest(
    @field:EnumValue(enumClass = SortCriteria::class, message = "유효하지 않은 정렬 타입입니다.")
    val sortBy: String = SortCriteria.POPULAR.name,
    @field:EnumValue(enumClass = ShootingSeason::class, message = "유효하지 않은 촬영시기가 존재합니다.")
    val availableSeasons: List<String> = emptyList(),
    @field:EnumValue(enumClass = CameraType::class, message = "유효하지 않은 카메라 종류가 존재합니다.")
    val cameraTypes: List<String> = emptyList(),
    @field:EnumValue(enumClass = RetouchStyle::class, message = "유효하지 않은 보정 스타일이 존재합니다.")
    val retouchStyles: List<String> = emptyList(),
    @field:EnumValue(enumClass = PartnerShopCategory::class, message = "유효하지 않은 패키지가 존재합니다.")
    val partnerShopCategories: List<String> = emptyList(),
    val minPrice: Long = 0L,
    val maxPrice: Long = 10_000_000L,
) {
    fun toQuery() =
        FilterUserLikesQuery(
            sortBy = SortCriteria.from(sortBy),
            availableSeasons = availableSeasons.map { ShootingSeason.from(it) }.toSet(),
            cameraTypes = cameraTypes.map { CameraType.from(it) }.toSet(),
            retouchStyles = retouchStyles.map { RetouchStyle.from(it) }.toSet(),
            partnerShopCategories = partnerShopCategories.map { PartnerShopCategory.from(it) }.toSet(),
            minPrice = minPrice,
            maxPrice = maxPrice,
        )
}
