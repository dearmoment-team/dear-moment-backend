package kr.kro.dearmoment.like.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.like.domain.StudioLike

data class GetStudioLikeResponse(
    @Schema(description = "좋아요 ID", example = "1")
    val likeId: Long,
    @Schema(description = "스튜디오 ID", example = "1")
    val studioId: Long,
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
    val availableSeasons: List<String>,
    @Schema(
        description = "보정 스타일",
        example = "[\"MODERN\", \"VINTAGE\"]",
    )
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
