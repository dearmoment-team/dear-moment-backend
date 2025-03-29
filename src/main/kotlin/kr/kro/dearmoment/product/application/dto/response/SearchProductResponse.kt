package kr.kro.dearmoment.product.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.product.domain.model.Product

@Schema(description = "상품 검색 응답 DTO")
data class SearchProductResponse(
    @Schema(description = "상품 ID", example = "100")
    val productId: Long,
    @Schema(description = "스튜디오 이름", example = "스튜디오 A")
    val studioName: String,
    @Schema(
        description = "스튜디오 썸네일 이미지 URL 목록",
        example = "[\"http://example.com/image1.jpg\", \"http://example.com/image2.jpg\"]",
    )
    val thumbnailUrls: List<String>,
    @Schema(
        description = "보정 스타일 목록 (도메인: RetouchStyle)",
        example = "[\"MODERN\", \"VINTAGE\"]",
    )
    val retouchStyles: List<String>,
    @Schema(
        description = "촬영 가능 시기 목록 (도메인: ShootingSeason)",
        example = "[\"YEAR_2025_FIRST_HALF\", \"YEAR_2025_SECOND_HALF\"]",
    )
    val shootingSeason: List<String>,
    @Schema(description = "최소 가격", example = "800000")
    val minPrice: Long,
    @Schema(description = "최대 가격", example = "1200000")
    val maxPrice: Long,
    @Schema(description = "할인율", example = "10")
    val discountRate: Int,
    @Schema(description = "사용자가 상품 좋아요를 눌렀는지 여부", example = "true")
    val isLiked: Boolean,
) {
    companion object {
        /***
         * TODO: 좋아요 여부, 할인율
         */
        fun from(product: Product): SearchProductResponse {
            requireNotNull(product.studio)

            return SearchProductResponse(
                productId = product.productId,
                studioName = product.studio.name,
                thumbnailUrls = product.extractThumbnailUrls(),
                retouchStyles = product.retouchStyles.map { it.name },
                shootingSeason = product.availableSeasons.map { it.name },
                minPrice = product.options.minOf { it.discountPrice },
                maxPrice = product.options.maxOf { it.discountPrice },
                discountRate = product.calculateDiscountRate(),
                isLiked = false,
            )
        }
    }
}
