package kr.kro.dearmoment.like.application.dto

import kr.kro.dearmoment.like.domain.ProductOptionLike

data class GetProductOptionLikeResponse(
    val likeId: Long,
    val productId: Long,
    val studioName: String,
    val optionName: String,
    val price: Long,
    val thumbnailUrl: String,
    val originalProvided: Boolean,
    val shootingHours: Int,
    val shootingLocationCount: Int,
    val costumeCount: Int,
    val retouchedCount: Int,
) {
    companion object {
        fun from(like: ProductOptionLike): GetProductOptionLikeResponse {
            val option = like.product.options.first { it.optionId == like.productOptionId }

            return GetProductOptionLikeResponse(
                likeId = like.id,
                productId = like.product.productId,
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
