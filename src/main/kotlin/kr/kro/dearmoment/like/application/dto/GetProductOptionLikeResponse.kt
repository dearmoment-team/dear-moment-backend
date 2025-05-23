package kr.kro.dearmoment.like.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.like.domain.ProductOptionLike

data class GetProductOptionLikeResponse(
    @Schema(description = "좋아요 ID", example = "1")
    val likeId: Long,
    @Schema(description = "상품 ID", example = "1")
    val productId: Long,
    @Schema(description = "상품 옵션 ID", example = "1")
    val productOptionId: Long,
    @Schema(description = "스튜디오 이름", example = "디어모먼트 스튜디오")
    val studioName: String,
    @Schema(description = "상품 옵션 이름", example = "Basic")
    val optionName: String,
    @Schema(description = "가격", example = "1000000")
    val price: Long,
    @Schema(description = "할인율", example = "25")
    val discountRate: Int,
    @Schema(description = "썸네일 이미지 url", example = "http://example.com/main.jpg")
    val thumbnailUrl: String,
    @Schema(description = "원본 제공 여부", example = "true")
    val originalProvided: Boolean,
    @Schema(
        description = "촬영 가능 시기 (도메인: ShootingSeason)",
        example = "[\"YEAR_2025_FIRST_HALF\", \"YEAR_2025_SECOND_HALF\"]",
    )
    val shootingSeason: List<String>,
    @Schema(description = "촬영 시간", example = "3")
    val shootingHours: Int,
    @Schema(description = "촬영 장소 수", example = "2")
    val shootingLocationCount: Int,
    @Schema(description = "의상 수", example = "10")
    val costumeCount: Int,
    @Schema(description = "보정 사진 회수", example = "30")
    val retouchedCount: Int,
) {
    companion object {
        fun from(like: ProductOptionLike): GetProductOptionLikeResponse {
            val option = like.product.options.first { it.optionId == like.productOptionId }

            return GetProductOptionLikeResponse(
                likeId = like.id,
                productId = like.product.productId,
                productOptionId = like.product.productId,
                studioName = like.studioName,
                optionName = like.product.title,
                price = option.discountPrice,
                discountRate = like.product.calculateDiscountRate(),
                thumbnailUrl = like.product.mainImage.url,
                originalProvided = like.product.options.any { it.originalProvided },
                shootingHours = option.shootingHours,
                shootingLocationCount = option.shootingLocationCount,
                shootingSeason = like.product.availableSeasons.map { it.name },
                costumeCount = option.costumeCount,
                retouchedCount = option.retouchedCount,
            )
        }
    }
}
