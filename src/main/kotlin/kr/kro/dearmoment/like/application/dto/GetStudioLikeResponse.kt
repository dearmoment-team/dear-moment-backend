package kr.kro.dearmoment.like.application.dto

import kr.kro.dearmoment.like.domain.StudioLike

data class GetStudioLikeResponse(
    val likeId: Long,
    val studioId: Long,
    val name: String,
    val thumbnailUrls: List<String>,
    val minPrice: Long,
    val maxPrice: Long,
    val availableSeasons: List<String>,
    val retouchStyles: List<String>,
) {
    companion object {
        fun from(like: StudioLike): GetStudioLikeResponse {
            check(like.studio.products.isNotEmpty())

            val product = like.studio.products[0]

            return GetStudioLikeResponse(
                likeId = like.id,
                studioId = like.studio.id,
                name = like.studio.name,
                thumbnailUrls = product.subImages.map { it.url },
                minPrice = product.options.minOf { it.originalPrice },
                maxPrice = product.options.minOf { it.originalPrice },
                availableSeasons = product.availableSeasons.map { it.name },
                retouchStyles = product.retouchStyles.map { it.name },
            )
        }
    }
}
