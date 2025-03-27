package kr.kro.dearmoment.product.application.dto.response

import kr.kro.dearmoment.product.domain.model.Product

data class SearchProductResponse(
    val productId: Long,
    val studioName: String,
    val thumbnailUrls: List<String>,
    val retouchStyles: List<String>,
    val shootingSeason: List<String>,
    val minPrice: Long,
    val maxPrice: Long,
    val discountRate: Int,
    val isLike: Boolean,
) {
    companion object {
        /***
         * TODO: 좋아요 여부, 할인율
         */
        fun from(product: Product): SearchProductResponse {
            requireNotNull(product.studio)

            val maxDiscountRate = product.options.maxOf { (it.discountPrice.toDouble() / it.originalPrice) * 100 }.toInt()

            return SearchProductResponse(
                productId = product.productId,
                studioName = product.studio.name,
                thumbnailUrls = product.subImages.map { it.url },
                retouchStyles = product.retouchStyles.map { it.name },
                shootingSeason = product.availableSeasons.map { it.name },
                minPrice = product.options.minOf { it.discountPrice },
                maxPrice = product.options.maxOf { it.discountPrice },
                discountRate = if (maxDiscountRate == 100) 0 else maxDiscountRate,
                isLike = false,
            )
        }
    }
}
