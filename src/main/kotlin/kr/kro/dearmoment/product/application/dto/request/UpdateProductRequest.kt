package kr.kro.dearmoment.product.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
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

/**
 * [상품 부분 수정] 시 사용하는 요청 DTO
 *
 * - 각 필드가 null이면 기존 값이 유지되고, 값이 있을 경우 유효성 검증이 수행됩니다.
 * - 서브/추가 이미지는 부분 업데이트를 위해 수정할 항목만 전달합니다.
 * - **사용자 ID는 클라이언트가 임의로 전달하는 대신, 인증 principal에서 받아 처리합니다.**
 */
@Schema(description = "상품 부분 수정 요청 DTO")
data class UpdateProductRequest(
    @Schema(description = "상품 ID", example = "100", required = true)
    @field:NotNull(message = "상품 ID는 필수입니다.")
    val productId: Long,
    @Schema(description = "스튜디오 ID", example = "100", required = true)
    @field:NotNull(message = "스튜디오 ID는 필수입니다.")
    val studioId: Long,
    @Schema(description = "상품 유형 (도메인: ProductType)", example = "WEDDING_SNAP", required = false)
    @field:EnumValue(enumClass = ProductType::class, message = "유효하지 상품 타입입니다.")
    val productType: String? = null,
    @Schema(description = "촬영 장소 (도메인: ShootingPlace)", example = "JEJU", required = false)
    @field:EnumValue(enumClass = ShootingPlace::class, message = "유효하지 촬영 장소입니다.")
    val shootingPlace: String? = null,
    @Schema(description = "상품 제목", example = "예쁜 웨딩 사진 촬영", required = false)
    val title: String? = null,
    @Schema(description = "상품 설명", example = "신랑, 신부의 아름다운 순간을 담은 웨딩 사진", required = false)
    val description: String? = null,
    @Schema(
        description = "촬영 가능 시기 (여러 값 가능, 도메인: ShootingSeason)",
        example = "[\"YEAR_2025_FIRST_HALF\", \"YEAR_2025_SECOND_HALF\"]",
        required = false,
    )
    @field:EnumValue(enumClass = ShootingSeason::class, message = "유효하지 촬영 시기가 존재합니다.")
    val availableSeasons: List<String>? = null,
    @Schema(
        description = "카메라 종류 (여러 값 가능, 도메인: CameraType)",
        example = "[\"DIGITAL\", \"FILM\"]",
        required = false,
    )
    @field:EnumValue(enumClass = CameraType::class, message = "유효하지 카메라 종류가 존재합니다.")
    val cameraTypes: List<String>? = null,
    @Schema(
        description = "보정 스타일 (여러 값 가능, 도메인: RetouchStyle)",
        example = "[\"MODERN\", \"VINTAGE\"]",
        required = false,
    )
    @field:EnumValue(enumClass = RetouchStyle::class, message = "유효하지 보정 스타일이 존재합니다.")
    val retouchStyles: List<String>? = null,
    @field:Valid
    @Schema(description = "수정할 서브 이미지 정보 목록 (부분 업데이트 가능, 각 항목에 인덱스와 액션 정보 포함)", required = false)
    val subImagesFinal: List<SubImageFinalRequest>? = null,
    @field:Valid
    @Schema(description = "최종 추가 이미지 목록 (0~최대 5개)", required = false)
    val additionalImagesFinal: List<AdditionalImageFinalRequest>? = null,
    @Schema(description = "상세 정보", example = "연락처: 010-1234-5678, 상세 문의는 이메일로", required = false)
    val detailedInfo: String? = null,
    @Schema(description = "연락처 정보", example = "010-1234-5678", required = false)
    val contactInfo: String? = null,
) {
    companion object {
        /**
         * 파일 업로드 처리 후 실제 [Image]가 결정된 상태에서
         * 최종 [Product] 도메인 객체를 생성하기 위한 매핑 메서드.
         */
        fun toDomain(
            req: UpdateProductRequest,
            existingProduct: Product,
            mainImage: Image,
            subImages: List<Image>,
            additionalImages: List<Image>,
            options: List<UpdateProductOptionRequest> = emptyList(),
            userId: UUID,
        ): Product {
            val productTypeEnum = req.productType?.let { ProductType.from(it) } ?: existingProduct.productType
            val shootingPlaceEnum = req.shootingPlace?.let { ShootingPlace.from(it) } ?: existingProduct.shootingPlace
            val seasonSet = req.availableSeasons?.map { ShootingSeason.from(it) }?.toSet() ?: existingProduct.availableSeasons
            val cameraSet = req.cameraTypes?.map { CameraType.from(it) }?.toSet() ?: existingProduct.cameraTypes
            val styleSet = req.retouchStyles?.map { RetouchStyle.from(it) }?.toSet() ?: existingProduct.retouchStyles

            val mainImg = mainImage.withUserId(userId)
            val subImgList = subImages.map { it.withUserId(userId) }
            val addImgList = additionalImages.map { it.withUserId(userId) }

            val domainOptions =
                if (options.isNotEmpty()) {
                    options.map { UpdateProductOptionRequest.toDomain(it, req.productId) }
                } else {
                    existingProduct.options
                }

            return Product(
                productId = req.productId,
                userId = userId,
                productType = productTypeEnum,
                shootingPlace = shootingPlaceEnum,
                title = req.title ?: existingProduct.title,
                availableSeasons = seasonSet,
                cameraTypes = cameraSet,
                retouchStyles = styleSet,
                mainImage = mainImg,
                subImages = subImgList,
                additionalImages = addImgList,
                options = domainOptions,
            )
        }
    }
}

/**
 * 서브 이미지 수정 액션 (KEEP: 기존 유지, DELETE: 삭제, UPLOAD: 새 파일 업로드)
 */
@Schema(description = "서브 이미지 수정 액션", allowableValues = ["KEEP", "DELETE", "UPLOAD"])
enum class UpdateSubImageAction {
    KEEP,
    DELETE,
    UPLOAD,
}

/**
 * 수정할 서브 이미지 정보를 담은 DTO
 */
@Schema(description = "수정할 서브 이미지 정보 DTO")
data class SubImageFinalRequest(
    @Schema(description = "서브 이미지 처리 액션", example = "KEEP", required = true)
    val action: UpdateSubImageAction,
    @Schema(description = "수정할 이미지 인덱스 (0부터 시작)", example = "2", required = true)
    val index: Int,
    @Schema(description = "기존 이미지 ID (KEEP/DELETE 시 필요)", example = "10", required = false)
    val imageId: Long? = null,
)

/**
 * 추가 이미지 수정 액션 (KEEP: 기존 유지, DELETE: 삭제, UPLOAD: 새 파일 업로드)
 */
@Schema(description = "추가 이미지 수정 액션", allowableValues = ["KEEP", "DELETE", "UPLOAD"])
enum class UpdateAdditionalImageAction {
    KEEP,
    DELETE,
    UPLOAD,
}

/**
 * "최종" 추가 이미지 정보를 담은 DTO
 */
@Schema(description = "최종 추가 이미지 정보 DTO")
data class AdditionalImageFinalRequest(
    @Schema(description = "추가 이미지 처리 액션", example = "UPLOAD", required = true)
    val action: UpdateAdditionalImageAction,
    @Schema(description = "기존 이미지 ID (KEEP/DELETE 시 필요)", example = "20")
    val imageId: Long?,
)

/**
 * [상품 옵션] 수정 요청 DTO
 */
@Schema(description = "[상품 옵션] 수정 요청 DTO")
data class UpdateProductOptionRequest(
    @Schema(description = "옵션 ID (옵션 삭제의 경우 null)", example = "0")
    val optionId: Long?,
    @field:NotBlank(message = "옵션명은 필수입니다.")
    @Schema(description = "옵션명", example = "옵션1", required = true)
    val name: String,
    @field:NotBlank(message = "옵션 타입은 필수입니다.")
    @Schema(
        description = "옵션 타입 (도메인: OptionType)",
        example = "SINGLE",
        allowableValues = ["SINGLE", "PACKAGE"],
        required = true,
    )
    val optionType: String,
    @Schema(description = "할인 적용 여부", example = "false")
    val discountAvailable: Boolean = false,
    @Schema(description = "정상 가격", example = "100000")
    val originalPrice: Long = 0,
    @Schema(description = "할인 가격", example = "80000")
    val discountPrice: Long = 0,
    @Schema(description = "옵션 설명", example = "옵션에 대한 상세 설명")
    val description: String? = null,
    // 단품용
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
    // 패키지용
    @field:Valid
    @Schema(description = "파트너샵 목록")
    val partnerShops: List<UpdatePartnerShopRequest> = emptyList(),
    @Schema(description = "선택 추가사항", example = "추가적인 옵션 상세 정보", required = false)
    val optionalAdditionalDetails: String? = null,
) {
    companion object {
        fun toDomain(
            dto: UpdateProductOptionRequest,
            productId: Long,
        ): ProductOption {
            val optionTypeEnum = OptionType.from(dto.optionType)
            return ProductOption(
                optionId = dto.optionId ?: 0L,
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
                partnerShops =
                    dto.partnerShops.map {
                        PartnerShop(
                            category = PartnerShopCategory.from(it.category),
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

/**
 * [파트너샵] 수정 요청 DTO
 */
@Schema(description = "[파트너샵] 수정 요청 DTO")
data class UpdatePartnerShopRequest(
    @field:NotBlank(message = "파트너샵 카테고리는 필수입니다.")
    @Schema(
        description = "파트너샵 카테고리 (도메인: PartnerShopCategory)",
        example = "HAIR_MAKEUP",
        allowableValues = ["HAIR_MAKEUP", "DRESS", "MENS_SUIT", "BOUQUET", "VIDEO", "STUDIO", "ETC"],
        required = true,
    )
    val category: String,
    @field:NotBlank(message = "파트너샵 이름은 필수입니다.")
    @Schema(description = "파트너샵 이름", example = "샘플샵", required = true)
    val name: String,
    @field:NotBlank(message = "파트너샵 링크는 필수입니다.")
    @Schema(description = "파트너샵 링크", example = "http://example.com", required = true)
    val link: String,
)
