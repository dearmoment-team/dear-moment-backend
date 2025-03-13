package kr.kro.dearmoment.product.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
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
import org.springframework.web.multipart.MultipartFile

/**
 * [상품 등록] 시 사용하는 요청 DTO
 */
@Schema(description = "상품 등록 요청 DTO")
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
    /**
     * 촬영 가능 시기
     */
    @Schema(
        description = "촬영 가능 시기 (여러 값 가능, 도메인: ShootingSeason)",
        example = "[\"YEAR_2025_FIRST_HALF\", \"YEAR_2025_SECOND_HALF\"]",
    )
    val availableSeasons: List<String> = emptyList(),
    /**
     * 카메라 종류
     */
    @Schema(
        description = "카메라 종류 (여러 값 가능, 도메인: CameraType)",
        example = "[\"DIGITAL\", \"FILM\"]",
    )
    val cameraTypes: List<String> = emptyList(),
    /**
     * 보정 스타일
     */
    @Schema(
        description = "보정 스타일 (여러 값 가능, 도메인: RetouchStyle)",
        example = "[\"MODERN\", \"VINTAGE\"]",
    )
    val retouchStyles: List<String> = emptyList(),
    /**
     * 대표 이미지(1장) - 실제 업로드 파일
     */
    @field:NotNull(message = "대표 이미지는 필수입니다.")
    @Schema(description = "대표 이미지 파일", required = true)
    val mainImageFile: MultipartFile? = null,
    /**
     * 서브 이미지(필수 4장)
     */
    @field:Size(min = 4, max = 4, message = "서브 이미지는 정확히 4장이어야 합니다.")
    @Schema(description = "서브 이미지 파일 목록 (정확히 4장)", required = true)
    val subImageFiles: List<MultipartFile> = emptyList(),
    /**
     * 추가 이미지(최대 5장)
     */
    @field:Size(max = 5, message = "추가 이미지는 최대 5장까지 등록 가능합니다.")
    @Schema(description = "추가 이미지 파일 목록 (최대 5장)")
    val additionalImageFiles: List<MultipartFile> = emptyList(),
    /**
     * 상세 정보, 연락처 등
     */
    @Schema(description = "상세 정보", example = "연락처: 010-1234-5678, 상세 문의는 이메일로")
    val detailedInfo: String? = null,
    @Schema(description = "연락처 정보", example = "010-1234-5678")
    val contactInfo: String? = null,
    /**
     * 옵션 목록
     */
    @Schema(description = "상품 옵션 목록")
    val options: List<CreateProductOptionRequest> = emptyList(),
) {
    companion object {
        fun toDomain(
            req: CreateProductRequest,
            mainImageUrl: String? = null,
            subImagesUrls: List<String> = emptyList(),
            additionalImagesUrls: List<String> = emptyList(),
        ): Product {
            val productTypeEnum =
                ProductType.entries.find { it.name == req.productType }
                    ?: throw IllegalArgumentException("Invalid productType: ${req.productType}")

            val shootingPlaceEnum =
                ShootingPlace.entries.find { it.name == req.shootingPlace }
                    ?: throw IllegalArgumentException("Invalid shootingPlace: ${req.shootingPlace}")

            val seasonSet = req.availableSeasons.map { ShootingSeason.valueOf(it) }.toSet()
            val cameraSet = req.cameraTypes.map { CameraType.valueOf(it) }.toSet()
            val styleSet = req.retouchStyles.map { RetouchStyle.valueOf(it) }.toSet()

            val mainImg =
                mainImageUrl?.let { url ->
                    Image(
                        userId = req.userId,
                        fileName = url.substringAfterLast('/'),
                        url = url,
                    )
                }

            val subImgList =
                subImagesUrls.map { url ->
                    Image(
                        userId = req.userId,
                        fileName = url.substringAfterLast('/'),
                        url = url,
                    )
                }

            val addImgList =
                additionalImagesUrls.map { url ->
                    Image(
                        userId = req.userId,
                        fileName = url.substringAfterLast('/'),
                        url = url,
                    )
                }

            val domainOptions = req.options.map { CreateProductOptionRequest.toDomain(it, 0L) }

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
                mainImage = mainImg ?: throw IllegalArgumentException("대표 이미지는 필수입니다."),
                subImages = subImgList,
                additionalImages = addImgList,
                detailedInfo = req.detailedInfo ?: "",
                contactInfo = req.contactInfo ?: "",
                options = domainOptions,
            )
        }
    }
}

/**
 * [상품 옵션] 생성 요청 DTO
 */
@Schema(description = "[상품 옵션] 생성 요청 DTO")
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
 * [파트너샵] 생성 요청 DTO
 */
@Schema(description = "[파트너샵] 생성 요청 DTO")
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
