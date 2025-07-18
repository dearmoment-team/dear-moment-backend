package kr.kro.dearmoment.product.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.product.domain.model.option.OptionType
import kr.kro.dearmoment.product.domain.model.option.PartnerShop
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.product.domain.model.option.ProductOption
import java.time.LocalDateTime

@Schema(description = "이미지 응답 DTO")
data class ImageResponse(
    @Schema(description = "이미지 ID", example = "1")
    val imageId: Long,
    @Schema(description = "이미지 URL", example = "http://example.com/main.jpg")
    val url: String,
) {
    companion object {
        fun fromDomain(img: Image): ImageResponse {
            return ImageResponse(
                imageId = img.imageId,
                url = img.url,
            )
        }
    }
}

@Schema(description = "상품 응답 DTO")
data class ProductResponse(
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
    val options: List<ProductOptionResponse>,
) {
    companion object {
        fun fromDomain(prod: Product): ProductResponse {
            return ProductResponse(
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
                options = prod.options.map { ProductOptionResponse.fromDomain(it) },
            )
        }
    }

    fun toDomain(): Product {
        return Product(
            productId = this.productId,
            productType = ProductType.valueOf(this.productType),
            shootingPlace = ShootingPlace.valueOf(this.shootingPlace),
            title = this.title,
            availableSeasons = this.availableSeasons.map { ShootingSeason.valueOf(it) }.toSet(),
            cameraTypes = this.cameraTypes.map { CameraType.valueOf(it) }.toSet(),
            retouchStyles = this.retouchStyles.map { RetouchStyle.valueOf(it) }.toSet(),
            mainImage =
                Image(
                    fileName = this.mainImage.url.substringAfterLast('/'),
                    url = this.mainImage.url,
                ),
            subImages =
                this.subImages.map { img ->
                    Image(
                        fileName = img.url.substringAfterLast('/'),
                        url = img.url,
                    )
                },
            additionalImages =
                this.additionalImages.map { img ->
                    Image(
                        fileName = img.url.substringAfterLast('/'),
                        url = img.url,
                    )
                },
            createdAt = this.createdAt ?: LocalDateTime.now(),
            updatedAt = this.updatedAt ?: LocalDateTime.now(),
            options = this.options.map { it.toDomain() },
        )
    }
}

@Schema(description = "[상품 옵션] 응답 DTO")
data class ProductOptionResponse(
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
    @Schema(description = "원본 제공 여부", example = "true")
    val originalProvided: Boolean = true,
    @Schema(description = "파트너샵 목록")
    val partnerShops: List<PartnerShopResponse>,
    @Schema(description = "생성 일시", example = "2025-03-09T12:00:00", nullable = true)
    val createdAt: LocalDateTime?,
    @Schema(description = "수정 일시", example = "2025-03-09T12:00:00", nullable = true)
    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun fromDomain(opt: ProductOption): ProductOptionResponse {
            return ProductOptionResponse(
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
                originalProvided = opt.originalProvided,
                partnerShops = opt.partnerShops.map { PartnerShopResponse.fromDomain(it) },
                createdAt = opt.createdAt,
                updatedAt = opt.updatedAt,
            )
        }
    }

    fun toDomain(): ProductOption {
        return ProductOption(
            optionId = this.optionId,
            productId = this.productId,
            name = this.name,
            optionType = OptionType.valueOf(this.optionType),
            discountAvailable = this.discountAvailable,
            originalPrice = this.originalPrice,
            discountPrice = this.discountPrice,
            description = this.description ?: "",
            costumeCount = this.costumeCount,
            shootingLocationCount = this.shootingLocationCount,
            shootingHours = this.shootingHours,
            shootingMinutes = this.shootingMinutes,
            retouchedCount = this.retouchedCount,
            originalProvided = this.originalProvided,
            partnerShops = this.partnerShops.map { it.toDomain() },
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
    }
}

@Schema(description = "[파트너샵] 응답 DTO")
data class PartnerShopResponse(
    @Schema(
        description = "파트너샵 카테고리 (도메인: PartnerShopCategory)",
        example = "HAIR_MAKEUP",
        allowableValues = ["HAIR_MAKEUP", "DRESS", "MENS_SUIT", "BOUQUET", "VIDEO", "STUDIO", "ETC"],
    )
    val category: String,
    @Schema(description = "파트너샵 이름", example = "샘플샵")
    val name: String,
    @Schema(description = "파트너샵 링크", example = "http://example.com")
    val link: String,
) {
    companion object {
        fun fromDomain(ps: PartnerShop): PartnerShopResponse {
            return PartnerShopResponse(
                category = ps.category.name,
                name = ps.name,
                link = ps.link,
            )
        }
    }

    fun toDomain(): PartnerShop {
        return PartnerShop(
            category = PartnerShopCategory.valueOf(this.category),
            name = this.name,
            link = this.link,
        )
    }
}
