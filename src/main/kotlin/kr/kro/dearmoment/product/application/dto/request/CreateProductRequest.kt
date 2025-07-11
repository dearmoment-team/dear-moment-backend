package kr.kro.dearmoment.product.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import kr.kro.dearmoment.common.validation.EnumValue
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.image.domain.withUserId
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
import java.util.UUID

data class CreateProductRequest(
    @Schema(description = "스튜디오 ID", example = "1", required = true)
    val studioId: Long,
    @Schema(description = "상품 유형", example = "WEDDING_SNAP", required = true)
    @field:NotBlank(message = "상품 유형은 필수입니다.")
    @field:EnumValue(enumClass = ProductType::class, message = "유효하지 상품 타입입니다.")
    val productType: String,
    @field:NotBlank(message = "촬영 장소는 필수입니다.")
    @Schema(description = "촬영 장소", example = "JEJU", required = true)
    @field:EnumValue(enumClass = ShootingPlace::class, message = "유효하지 촬영 장소입니다.")
    val shootingPlace: String,
    @Schema(description = "상품 제목", example = "예쁜 웨딩 사진 촬영", required = true)
    @field:NotBlank(message = "상품 제목은 비어 있을 수 없습니다.")
    val title: String,
    @Schema(description = "상품 설명", example = "신랑, 신부의 아름다운 순간을 담은 웨딩 사진")
    val description: String? = null,
    @Schema(
        description = "촬영 가능 시기 (여러 값 가능, 도메인: ShootingSeason)",
        example = "[\"YEAR_2025_FIRST_HALF\", \"YEAR_2025_SECOND_HALF\"]",
    )
    @field:EnumValue(enumClass = ShootingSeason::class, message = "유효하지 촬영 시기가 존재합니다.")
    val availableSeasons: List<String> = emptyList(),
    @Schema(
        description = "카메라 종류 (여러 값 가능, 도메인: CameraType)",
        example = "[\"DIGITAL\", \"FILM\"]",
    )
    @field:EnumValue(enumClass = CameraType::class, message = "유효하지 카메라 종류가 존재합니다.")
    val cameraTypes: List<String> = emptyList(),
    @Schema(
        description = "보정 스타일 (여러 값 가능, 도메인: RetouchStyle)",
        example = "[\"MODERN\", \"VINTAGE\"]",
    )
    @field:EnumValue(enumClass = RetouchStyle::class, message = "유효하지 보정 스타일이 존재합니다.")
    val retouchStyles: List<String> = emptyList(),
    @Schema(description = "상세 정보", example = "연락처: 010-1234-5678, 상세 문의는 이메일로")
    val detailedInfo: String? = null,
    @Schema(description = "연락처 정보", example = "010-1234-5678")
    val contactInfo: String? = null,
    @Schema(description = "상품 옵션 목록")
    val options: List<CreateProductOptionRequest> = emptyList(),
) {
    companion object {
        fun toDomain(
            req: CreateProductRequest,
            userId: UUID,
            mainImage: Image,
            subImages: List<Image>,
            additionalImages: List<Image>,
        ): Product {
            val productTypeEnum = ProductType.from(req.productType)
            val shootingPlaceEnum = ShootingPlace.from(req.shootingPlace)
            val seasonSet = req.availableSeasons.map { ShootingSeason.from(it) }.toSet()
            val cameraSet = req.cameraTypes.map { CameraType.from(it) }.toSet()
            val styleSet = req.retouchStyles.map { RetouchStyle.from(it) }.toSet()
            val mainImg = mainImage.withUserId(userId)
            val subImgList = subImages.map { it.withUserId(userId) }
            val addImgList = additionalImages.map { it.withUserId(userId) }
            val optionList = req.options.map { CreateProductOptionRequest.toDomain(it, 0L) }

            return Product(
                productId = 0L,
                userId = userId,
                productType = productTypeEnum,
                shootingPlace = shootingPlaceEnum,
                title = req.title,
                availableSeasons = seasonSet,
                cameraTypes = cameraSet,
                retouchStyles = styleSet,
                mainImage = mainImg,
                subImages = subImgList,
                additionalImages = addImgList,
                options = optionList,
            )
        }
    }
}

data class CreateProductOptionRequest(
    @field:NotBlank(message = "옵션명은 필수입니다.")
    @Schema(description = "옵션명", example = "옵션1", required = true)
    val name: String,
    @field:NotBlank(message = "옵션 타입은 필수입니다.")
    @Schema(description = "옵션 타입 (도메인: OptionType)", example = "SINGLE", required = true)
    @field:EnumValue(enumClass = OptionType::class, message = "유효하지 옵션 타입입니다.")
    val optionType: String,
    @Schema(description = "할인 적용 여부", example = "false")
    val discountAvailable: Boolean = false,
    @Schema(description = "정상 가격", example = "100000")
    val originalPrice: Long = 0,
    @Schema(description = "할인 가격", example = "80000")
    val discountPrice: Long = 0,
    @Schema(description = "옵션 설명", example = "옵션에 대한 상세 설명")
    val description: String? = null,
    @Schema(description = "의상 수량 (단품인 경우 1 이상)", example = "1")
    val costumeCount: Int = 0,
    @Schema(description = "촬영 장소 수 (단품인 경우 1 이상)", example = "1")
    val shootingLocationCount: Int = 0,
    @Schema(description = "촬영 시간 (시)", example = "2")
    val shootingHours: Int = 0,
    @Schema(description = "촬영 시간 (분)", example = "30")
    val shootingMinutes: Int = 0,
    @Schema(description = "보정된 사진 수 (단품인 경우 1 이상)", example = "1")
    val retouchedCount: Int = 0,
    @Schema(description = "원본 제공 여부", example = "true")
    val originalProvided: Boolean = false,
    @Schema(description = "파트너샵 목록")
    val partnerShops: List<CreatePartnerShopRequest> = emptyList(),
    @Schema(description = "선택 추가사항", example = "추가적인 옵션 상세 정보", required = false)
    val optionalAdditionalDetails: String? = null,
) {
    companion object {
        fun toDomain(
            dto: CreateProductOptionRequest,
            productId: Long,
        ): ProductOption {
            val optionTypeEnum = OptionType.from(dto.optionType)
            return ProductOption(
                optionId = 0L,
                productId = productId,
                name = dto.name,
                optionType = optionTypeEnum,
                discountAvailable = dto.discountAvailable,
                originalPrice = dto.originalPrice,
                discountPrice = dto.discountPrice,
                description = dto.description ?: " ",
                costumeCount = dto.costumeCount,
                shootingLocationCount = dto.shootingLocationCount,
                shootingHours = dto.shootingHours,
                shootingMinutes = dto.shootingMinutes,
                retouchedCount = dto.retouchedCount,
                originalProvided = dto.originalProvided,
                partnerShops =
                    dto.partnerShops.map {
                        PartnerShop(
                            category = PartnerShopCategory.valueOf(it.category),
                            name = it.name,
                            link = it.link,
                        )
                    },
                optionalAdditionalDetails = dto.optionalAdditionalDetails ?: " ",
                createdAt = null,
                updatedAt = null,
            )
        }
    }
}

data class CreatePartnerShopRequest(
    @Schema(
        description = "파트너샵 카테고리 (도메인: PartnerShopCategory)",
        required = true,
        allowableValues = ["HAIR_MAKEUP", "DRESS", "MENS_SUIT", "BOUQUET", "VIDEO", "STUDIO", "ETC"],
        example = "HAIR_MAKEUP",
    )
    @field:EnumValue(enumClass = PartnerShopCategory::class, message = "유효하지 제휴 업체입니다.")
    val category: String,
    @Schema(description = "파트너샵 이름", example = "샘플샵", required = true)
    val name: String,
    @Schema(description = "파트너샵 링크", example = "http://example.com", required = true)
    val link: String,
)
