package kr.kro.dearmoment.product.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.*
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode

data class CreateProductRequest(
    @Schema(description = "사용자 ID", example = "1", required = true)
    val userId: Long,
    @field:NotBlank(message = "상품 유형은 필수입니다.")
    @Schema(description = "상품 유형", example = "WEDDING_SNAP", required = true)
    val productType: String,
    @field:NotBlank(message = "촬영 장소는 필수입니다.")
    @Schema(description = "촬영 장소", example = "JEJU", required = true)
    val shootingPlace: String,
    @field:NotBlank(message = "상품 제목은 비어 있을 수 없습니다.")
    @Schema(description = "상품 제목", example = "예쁜 웨딩 사진 촬영", required = true)
    val title: String,
    @Schema(description = "상품 설명", example = "신랑, 신부의 아름다운 순간을 담은 웨딩 사진")
    val description: String? = null,
    @Schema(
        description = "촬영 가능 시기 (여러 값 가능, 도메인: ShootingSeason)",
        example = "[\"YEAR_2025_FIRST_HALF\", \"YEAR_2025_SECOND_HALF\"]",
    )
    val availableSeasons: List<String> = emptyList(),
    @Schema(
        description = "카메라 종류 (여러 값 가능, 도메인: CameraType)",
        example = "[\"DIGITAL\", \"FILM\"]",
    )
    val cameraTypes: List<String> = emptyList(),
    @Schema(
        description = "보정 스타일 (여러 값 가능, 도메인: RetouchStyle)",
        example = "[\"MODERN\", \"VINTAGE\"]",
    )
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
            mainImage: Image,
            subImages: List<Image>,
            additionalImages: List<Image>,
        ): Product {
            val productTypeEnum =
                ProductType.entries.find { it.name == req.productType }
                    ?: throw CustomException(ErrorCode.INVALID_PRODUCT_TYPE)

            val shootingPlaceEnum =
                ShootingPlace.entries.find { it.name == req.shootingPlace }
                    ?: throw CustomException(ErrorCode.INVALID_SHOOTING_PLACE)

            val seasonSet =
                req.availableSeasons.map { season ->
                    try {
                        ShootingSeason.valueOf(season)
                    } catch (e: IllegalArgumentException) {
                        throw CustomException(ErrorCode.INVALID_SEASON)
                    }
                }.toSet()

            val cameraSet =
                req.cameraTypes.map { type ->
                    try {
                        CameraType.valueOf(type)
                    } catch (e: IllegalArgumentException) {
                        throw CustomException(ErrorCode.INVALID_CAMERA_TYPE)
                    }
                }.toSet()

            val styleSet =
                req.retouchStyles.map { style ->
                    try {
                        RetouchStyle.valueOf(style)
                    } catch (e: IllegalArgumentException) {
                        throw CustomException(ErrorCode.INVALID_RETOUCH_STYLE)
                    }
                }.toSet()

            // 메인 이미지 생성 (반드시 대표 이미지가 존재해야 함)
            val mainImg = Image(
                userId = req.userId,
                imageId = mainImage.imageId,
                fileName = mainImage.fileName,
                parId = mainImage.parId,
                url = mainImage.url,
            )

            val subImgList = subImages.map { image ->
                Image(
                    userId = req.userId,
                    imageId = image.imageId,
                    fileName = image.fileName,
                    parId = image.parId,
                    url = image.url,
                )
            }

            val addImgList = additionalImages.map { image ->
                Image(
                    userId = req.userId,
                    imageId = image.imageId,
                    fileName = image.fileName,
                    parId = image.parId,
                    url = image.url,
                )
            }

            // 옵션 요청을 도메인 모델로 변환하여 포함
            val optionList = req.options.map { CreateProductOptionRequest.toDomain(it, 0L) }

            return Product(
                productId = 0L,
                userId = req.userId,
                productType = productTypeEnum,
                shootingPlace = shootingPlaceEnum,
                title = req.title,
                description = req.description ?: "",
                availableSeasons = seasonSet,
                cameraTypes = cameraSet,
                retouchStyles = styleSet,
                mainImage = mainImg,
                subImages = subImgList,
                additionalImages = addImgList,
                detailedInfo = req.detailedInfo ?: "",
                contactInfo = req.contactInfo ?: "",
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
) {
    companion object {
        fun toDomain(
            dto: CreateProductOptionRequest,
            productId: Long,
        ): ProductOption {
            val optionTypeEnum = OptionType.valueOf(dto.optionType)
            return ProductOption(
                optionId = 0L,
                productId = productId,
                name = dto.name,
                optionType = optionTypeEnum,
                discountAvailable = dto.discountAvailable,
                originalPrice = dto.originalPrice,
                discountPrice = dto.discountPrice,
                description = dto.description ?: "",
                costumeCount = dto.costumeCount,
                shootingLocationCount = dto.shootingLocationCount,
                shootingHours = dto.shootingHours,
                shootingMinutes = dto.shootingMinutes,
                retouchedCount = dto.retouchedCount,
                originalProvided = dto.originalProvided,
                partnerShops = dto.partnerShops.map {
                    PartnerShop(
                        category = PartnerShopCategory.valueOf(it.category),
                        name = it.name,
                        link = it.link,
                    )
                },
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
    val category: String,
    @Schema(description = "파트너샵 이름", example = "샘플샵", required = true)
    val name: String,
    @Schema(description = "파트너샵 링크", example = "http://example.com", required = true)
    val link: String,
)
