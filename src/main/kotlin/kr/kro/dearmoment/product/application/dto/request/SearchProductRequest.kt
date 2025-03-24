package kr.kro.dearmoment.product.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.common.validation.EnumValue
import kr.kro.dearmoment.product.application.query.ProductSortCriteria
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory

@Schema(description = "상품 검색 요청 DTO")
data class SearchProductRequest(
    @field:EnumValue(enumClass = ProductSortCriteria::class, message = "유효하지 않은 정렬 타입입니다.")
    val sortBy: String = ProductSortCriteria.RECOMMENDED.name,
    @field:EnumValue(enumClass = ShootingSeason::class, message = "유효하지 않은 촬영시기가 존재합니다.")
    val availableSeasons: List<String>,
    @field:EnumValue(enumClass = CameraType::class, message = "유효하지 않은 카메라 종류가 존재합니다.")
    val cameraTypes: List<String>,
    @field:EnumValue(enumClass = RetouchStyle::class, message = "유효하지 않은 보정 스타일이 존재합니다.")
    val retouchStyles: List<String>,
    @field:EnumValue(enumClass = PartnerShopCategory::class, message = "유효하지 않은 패키지가 존재합니다.")
    val partnerShopCategories: List<String>,
    val minPrice: Long,
    val maxPrice: Long,
)
