package kr.kro.dearmoment.product.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.kro.dearmoment.common.validation.NotBlankIfPresent
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.OptionType
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.PartnerShopCategory
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason

/**
 * [상품 부분 수정] 시 사용하는 요청 DTO
 *
 * - 각 필드가 null이면 기존 값이 유지되고, 값이 있을 경우 유효성 검증이 수행됩니다.
 * - 서브/추가 이미지는 부분 업데이트를 위해 수정할 항목만 전달합니다.
 */
@Schema(description = "상품 부분 수정 요청 DTO")
data class UpdateProductRequest(
    @field:NotNull(message = "상품 ID는 필수입니다.")
    @Schema(description = "상품 ID", example = "100", required = true)
    val productId: Long,
    @field:NotNull(message = "스튜디오 ID는 필수입니다.")
    @Schema(description = "스튜디오 ID", example = "100", required = true)
    val studioId: Long,
    @field:NotNull(message = "사용자 ID는 필수입니다.")
    @Schema(description = "사용자 ID", example = "1", required = true)
    val userId: Long,
    @Schema(description = "상품 유형 (도메인: ProductType)", example = "WEDDING_SNAP", required = false)
    val productType: String? = null,
    @Schema(description = "촬영 장소 (도메인: ShootingPlace)", example = "JEJU", required = false)
    val shootingPlace: String? = null,
    @field:NotBlankIfPresent(message = "상품 제목이 빈 값일 수 없습니다.")
    @Schema(description = "상품 제목", example = "예쁜 웨딩 사진 촬영", required = false)
    val title: String? = null,
    @field:NotBlankIfPresent(message = "상품 설명이 빈 값일 수 없습니다.")
    @Schema(description = "상품 설명", example = "신랑, 신부의 아름다운 순간을 담은 웨딩 사진", required = false)
    val description: String? = null,
    @Schema(
        description = "촬영 가능 시기 (여러 값 가능, 도메인: ShootingSeason)",
        example = "[\"YEAR_2025_FIRST_HALF\", \"YEAR_2025_SECOND_HALF\"]",
        required = false,
    )
    val availableSeasons: List<String>? = null,
    @Schema(
        description = "카메라 종류 (여러 값 가능, 도메인: CameraType)",
        example = "[\"DIGITAL\", \"FILM\"]",
        required = false,
    )
    val cameraTypes: List<String>? = null,
    @Schema(
        description = "보정 스타일 (여러 값 가능, 도메인: RetouchStyle)",
        example = "[\"MODERN\", \"VINTAGE\"]",
        required = false,
    )
    val retouchStyles: List<String>? = null,
    @field:Valid
    @Schema(description = "수정할 서브 이미지 정보 목록 (부분 업데이트 가능, 각 항목에 인덱스와 액션 정보 포함)", required = false)
    val subImagesFinal: List<SubImageFinalRequest>? = null,
    @field:Valid
    @Schema(description = "최종 추가 이미지 목록 (0~최대 5개)", required = false)
    val additionalImagesFinal: List<AdditionalImageFinalRequest>? = null,
    @field:NotBlankIfPresent(message = "상세 정보가 빈 값일 수 없습니다.")
    @Schema(description = "상세 정보", example = "연락처: 010-1234-5678, 상세 문의는 이메일로", required = false)
    val detailedInfo: String? = null,
    @field:NotBlankIfPresent(message = "연락처 정보가 빈 값일 수 없습니다.")
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
            userId: Long = req.userId,
        ): Product {
            val productTypeEnum = req.productType?.let { ProductType.valueOf(it) } ?: existingProduct.productType
            val shootingPlaceEnum = req.shootingPlace?.let { ShootingPlace.valueOf(it) } ?: existingProduct.shootingPlace
            val seasonSet = req.availableSeasons?.map { ShootingSeason.valueOf(it) }?.toSet() ?: existingProduct.availableSeasons
            val cameraSet = req.cameraTypes?.map { CameraType.valueOf(it) }?.toSet() ?: existingProduct.cameraTypes
            val styleSet = req.retouchStyles?.map { RetouchStyle.valueOf(it) }?.toSet() ?: existingProduct.retouchStyles

            val mainImg =
                Image(
                    userId = userId,
                    imageId = mainImage.imageId,
                    fileName = mainImage.fileName,
                    parId = mainImage.parId,
                    url = mainImage.url,
                )

            val subImgList =
                subImages.map { image ->
                    Image(
                        userId = userId,
                        imageId = image.imageId,
                        fileName = image.fileName,
                        parId = image.parId,
                        url = image.url,
                    )
                }

            val addImgList =
                additionalImages.map { image ->
                    Image(
                        userId = userId,
                        imageId = image.imageId,
                        fileName = image.fileName,
                        parId = image.parId,
                        url = image.url,
                    )
                }

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
                description = req.description ?: existingProduct.description,
                availableSeasons = seasonSet,
                cameraTypes = cameraSet,
                retouchStyles = styleSet,
                mainImage = mainImg,
                subImages = subImgList,
                additionalImages = addImgList,
                detailedInfo = req.detailedInfo ?: existingProduct.detailedInfo,
                contactInfo = req.contactInfo ?: existingProduct.contactInfo,
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
) {
    companion object {
        fun toDomain(
            dto: UpdateProductOptionRequest,
            productId: Long,
        ): ProductOption {
            val optionTypeEnum = OptionType.valueOf(dto.optionType)
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
