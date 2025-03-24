package kr.kro.dearmoment.like.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.common.validation.EnumValue
import kr.kro.dearmoment.like.domain.ProductLike
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason

data class GetProductLikeResponse(
    @Schema(description = "좋아요 ID", example = "1")
    val likeId: Long,
    @Schema(description = "상품 ID", example = "1")
    val productId: Long,
    @Schema(description = "스튜디오 이름", example = "디어모먼트 스튜디오")
    val name: String,
    @Schema(description = "썸네일 이미지 url", example = "http://example.com/main.jpg")
    val thumbnailUrls: List<String>,
    @Schema(description = "최소 가격", example = "10000")
    val minPrice: Long,
    @Schema(description = "최대 가격", example = "200000")
    val maxPrice: Long,
    @Schema(
        description = "촬영 가능 시기",
        example = "[\"YEAR_2025_FIRST_HALF\", \"YEAR_2025_SECOND_HALF\"]",
    )
    @field:EnumValue(enumClass = ShootingSeason::class, message = "유효하지 않은 촬영 시기가 존재합니다.")
    val availableSeasons: List<String>,
    @Schema(
        description = "보정 스타일",
        example = "[\"MODERN\", \"VINTAGE\"]",
    )
    @field:EnumValue(enumClass = RetouchStyle::class, message = "유효하지 않은 보정 스타일이 존재합니다.")
    val retouchStyles: List<String>,
) {
    companion object {
        fun from(like: ProductLike): GetProductLikeResponse {
            val studio = like.product.studio ?: throw CustomException(ErrorCode.STUDIO_NOT_FOUND)
            val product = like.product

            return GetProductLikeResponse(
                likeId = like.id,
                productId = product.productId,
                name = studio.name,
                thumbnailUrls = product.subImages.map { it.url },
                minPrice = product.options.minOf { it.originalPrice },
                maxPrice = product.options.minOf { it.originalPrice },
                availableSeasons = product.availableSeasons.map { it.name },
                retouchStyles = product.retouchStyles.map { it.name },
            )
        }
    }
}
