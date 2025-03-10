package kr.kro.dearmoment.like.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.like.domain.ProductOptionLike

data class GetProductOptionLikeResponse(
    @Schema(description = "좋아요 ID", example = "1")
    val likeId: Long,
    @Schema(description = "상품 옵션 ID", example = "1")
    val productOptionId: Long,
    @Schema(description = "스튜디오 이름", example = "디어모먼트 스튜디오")
    val studioName: String,
    @Schema(description = "상품 옵션 이름", example = "Basic")
    val optionName: String,
    @Schema(description = "가격", example = "1000000")
    val price: Long,
    @Schema(description = "썸네일 이미지 url", example = "http://example.com/main.jpg")
    val thumbnailUrl: String,
    @Schema(description = "원본 제공 여부", example = "true")
    val originalProvided: Boolean,
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
                productOptionId = like.product.productId,
                studioName = like.studioName,
                optionName = like.product.title,
                price = option.originalPrice,
                thumbnailUrl = like.product.mainImage.url,
                originalProvided = like.product.options.any { it.originalProvided },
                shootingHours = option.shootingHours,
                shootingLocationCount = option.shootingLocationCount,
                costumeCount = option.costumeCount,
                retouchedCount = option.retouchedCount,
            )
        }
    }
}
