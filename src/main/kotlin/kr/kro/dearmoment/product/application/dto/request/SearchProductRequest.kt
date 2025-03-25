package kr.kro.dearmoment.product.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.common.validation.EnumValue
import kr.kro.dearmoment.product.application.dto.query.SearchProductQuery
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.product.domain.sort.SortCriteria

@Schema(description = "상품 검색 요청 DTO")
data class SearchProductRequest(
    @Schema(
        description = "정렬 기준 (기본 값: \"RECOMMENDED\")",
        allowableValues = ["RECOMMENDED", "POPULAR", "PRICE_LOW", "PRICE_HIGH"],
    )
    @field:EnumValue(enumClass = SortCriteria::class, message = "유효하지 않은 정렬 타입입니다.")
    val sortBy: String = SortCriteria.POPULAR.name,
    @Schema(
        description = "촬영 가능 시기",
        allowableValues =
            ["YEAR_2025_FIRST_HALF", "YEAR_2025_SECOND_HALF", "YEAR_2026_FIRST_HALF", "YEAR_2026_SECOND_HALF"],
    )
    @field:EnumValue(enumClass = ShootingSeason::class, message = "유효하지 않은 촬영시기가 존재합니다.")
    val availableSeasons: List<String> = emptyList(),
    @Schema(
        description = "카메라 종류",
        allowableValues = ["DIGITAL", "FILM"],
    )
    @field:EnumValue(enumClass = CameraType::class, message = "유효하지 않은 카메라 종류가 존재합니다.")
    val cameraTypes: List<String> = emptyList(),
    @Schema(
        description = "보정 스타일",
        allowableValues = [
            "MODERN", "CHIC", "CALM", "VINTAGE",
            "FAIRYTALE", "WARM", "DREAMY", "BRIGHT", "NATURAL",
        ],
    )
    @field:EnumValue(enumClass = RetouchStyle::class, message = "유효하지 않은 보정 스타일이 존재합니다.")
    val retouchStyles: List<String> = emptyList(),
    @Schema(
        description = "제휴 업체",
        allowableValues = ["HAIR_MAKEUP", "DRESS", "MENS_SUIT", "BOUQUET", "VIDEO", "STUDIO", "ETC"],
    )
    @field:EnumValue(enumClass = PartnerShopCategory::class, message = "유효하지 않은 패키지가 존재합니다.")
    val partnerShopCategories: List<String> = emptyList(),
    val minPrice: Long = 0L,
    val maxPrice: Long = 3_000_000L,
) {
    fun toQuery() =
        SearchProductQuery(
            sortBy = SortCriteria.from(sortBy),
            availableSeasons = availableSeasons.map { ShootingSeason.from(it) },
            cameraTypes = cameraTypes.map { CameraType.from(it) },
            retouchStyles = retouchStyles.map { RetouchStyle.from(it) },
            partnerShopCategories = partnerShopCategories.map { PartnerShopCategory.from(it) },
            minPrice = minPrice,
            maxPrice = maxPrice,
        )
}
