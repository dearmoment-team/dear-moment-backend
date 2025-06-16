package kr.kro.dearmoment.product.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.studio.application.dto.response.ProductStudioResponse
import java.time.LocalDateTime

@Schema(description = "상품 응답 DTO")
data class GetProductResponse(
    @Schema(description = "상품 ID", example = "100")
    val productId: Long,
    @Schema(
        description = "상품 유형 (도메인: ProductType)",
        example = "WEDDING_SNAP",
        allowableValues = ["WEDDING_SNAP"],
    )
    val productType: String,
    @Schema(
        description = "촬영 장소 (도메인: ShootingPlace)",
        example = "JEJU",
        allowableValues = ["JEJU"],
    )
    val shootingPlace: String,
    @Schema(description = "상품 제목", example = "예쁜 웨딩 사진 촬영")
    val title: String,
    @Schema(
        description = "촬영 가능 시기 (도메인: ShootingSeason)",
        example = "[\"YEAR_2025_FIRST_HALF\", \"YEAR_2025_SECOND_HALF\"]",
    )
    val availableSeasons: List<String>,
    @Schema(
        description = "카메라 종류 (도메인: CameraType)",
        example = "[\"DIGITAL\", \"FILM\"]",
    )
    val cameraTypes: List<String>,
    @Schema(
        description = "보정 스타일 (도메인: RetouchStyle)",
        example = "[\"MODERN\", \"VINTAGE\"]",
    )
    val retouchStyles: List<String>,
    @Schema(description = "대표 이미지", example = "{\"imageId\": 1, \"url\": \"http://example.com/main.jpg\"}")
    val mainImage: ImageResponse,
    @Schema(
        description = "서브 이미지 목록",
        example =
            "[{\"imageId\": 2, \"url\": \"http://example.com/sub1.jpg\"}, " +
                "{\"imageId\": 3, \"url\": \"http://example.com/sub2.jpg\"}]",
    )
    val subImages: List<ImageResponse>,
    @Schema(
        description = "추가 이미지 목록",
        example =
            "[{\"imageId\": 4, \"url\": \"http://example.com/add1.jpg\"}, " +
                "{\"imageId\": 5, \"url\": \"http://example.com/add2.jpg\"}]",
    )
    val additionalImages: List<ImageResponse>,
    @Schema(description = "생성 일시", example = "2025-03-09T12:00:00", nullable = true)
    val createdAt: LocalDateTime?,
    @Schema(description = "수정 일시", example = "2025-03-09T12:00:00", nullable = true)
    val updatedAt: LocalDateTime?,
    @Schema(description = "상품 옵션 목록")
    val options: List<GetProductOptionResponse>,
    val studio: ProductStudioResponse,
    @Schema(description = "상품 좋아요 ID", example = "1", nullable = false)
    val likeId: Long,
) {
    companion object {
        fun fromDomain(
            prod: Product,
            productLikeId: Long = 0L,
            userOptionLikes: Map<Long, Long> = emptyMap(),
        ): GetProductResponse {
            requireNotNull(prod.studio)

            return GetProductResponse(
                productId = prod.productId,
                productType = prod.productType.name,
                shootingPlace = prod.shootingPlace.name,
                title = prod.title,
                availableSeasons = prod.availableSeasons.map { it.name },
                cameraTypes = prod.cameraTypes.map { it.name },
                retouchStyles = prod.retouchStyles.map { it.name },
                mainImage = ImageResponse.fromDomain(prod.mainImage),
                subImages = prod.subImages.map { ImageResponse.fromDomain(it) },
                additionalImages = prod.additionalImages.map { ImageResponse.fromDomain(it) },
                createdAt = prod.createdAt,
                updatedAt = prod.updatedAt,
                options = prod.options.map { GetProductOptionResponse.fromDomain(it, userOptionLikes) },
                studio = ProductStudioResponse.from(prod.studio),
                likeId = productLikeId,
            )
        }
    }
}
