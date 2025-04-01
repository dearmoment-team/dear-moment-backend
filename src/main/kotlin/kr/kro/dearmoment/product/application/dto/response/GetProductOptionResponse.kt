package kr.kro.dearmoment.product.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.product.domain.model.option.ProductOption
import java.time.LocalDateTime

@Schema(description = "[상품 옵션] 응답 DTO")
data class GetProductOptionResponse(
    @Schema(description = "옵션 ID", example = "10")
    val optionId: Long,
    @Schema(description = "상품 ID", example = "100")
    val productId: Long,
    @Schema(description = "옵션명", example = "옵션1")
    val name: String,
    @Schema(
        description = "옵션 타입 (도메인: OptionType)",
        example = "SINGLE",
        allowableValues = ["SINGLE", "PACKAGE"],
    )
    val optionType: String,
    @Schema(description = "할인 적용 여부", example = "false")
    val discountAvailable: Boolean,
    @Schema(description = "정상 가격", example = "100000")
    val originalPrice: Long,
    @Schema(description = "할인 가격", example = "80000")
    val discountPrice: Long,
    @Schema(description = "옵션 설명", example = "옵션에 대한 상세 설명", nullable = true)
    val description: String?,
    @Schema(description = "의상 수량", example = "1")
    val costumeCount: Int,
    @Schema(description = "촬영 장소 수", example = "1")
    val shootingLocationCount: Int,
    @Schema(description = "촬영 시간 (시)", example = "2")
    val shootingHours: Int,
    @Schema(description = "촬영 시간 (분)", example = "30")
    val shootingMinutes: Int,
    @Schema(description = "보정된 사진 수", example = "1")
    val retouchedCount: Int,
    @Schema(description = "파트너샵 목록")
    val partnerShops: List<PartnerShopResponse>,
    @Schema(description = "생성 일시", example = "2025-03-09T12:00:00", nullable = true)
    val createdAt: LocalDateTime?,
    @Schema(description = "수정 일시", example = "2025-03-09T12:00:00", nullable = true)
    val updatedAt: LocalDateTime?,
    @Schema(description = "좋아요 여부", example = "false", nullable = false)
    val isLiked: Boolean,
) {
    companion object {
        fun fromDomain(
            opt: ProductOption,
            userOptionLikes: Set<Long> = emptySet(),
        ): GetProductOptionResponse {
            return GetProductOptionResponse(
                optionId = opt.optionId,
                productId = opt.productId,
                name = opt.name,
                optionType = opt.optionType.name,
                discountAvailable = opt.discountAvailable,
                originalPrice = opt.originalPrice,
                discountPrice = opt.discountPrice,
                description = opt.description.takeIf { it.isNotBlank() },
                costumeCount = opt.costumeCount,
                shootingLocationCount = opt.shootingLocationCount,
                shootingHours = opt.shootingHours,
                shootingMinutes = opt.shootingMinutes,
                retouchedCount = opt.retouchedCount,
                partnerShops = opt.partnerShops.map { PartnerShopResponse.fromDomain(it) },
                createdAt = opt.createdAt,
                updatedAt = opt.updatedAt,
                isLiked = userOptionLikes.contains(opt.optionId),
            )
        }
    }
}
