package kr.kro.dearmoment.product.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
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
 * [상품 수정] 시 사용하는 요청 DTO
 *
 * - 서브 이미지는 `subImagesFinal`로 총 4개를 전달받으며,
 *   각 항목에 [action], [imageId], [newFile]을 조합하여 최종 상태를 나타냅니다.
 * - 추가 이미지는 `additionalImagesFinal`로 최대 5개를 전달받으며,
 *   서브 이미지와 동일하게 [action], [imageId], [newFile] 조합을 사용합니다.
 *
 * ### action 사용 예시
 * - KEEP: 기존 이미지를 그대로 사용 (imageId != null, newFile = null)
 * - DELETE: 기존 이미지를 삭제 (imageId != null, newFile = null)
 * - UPLOAD: 새 이미지를 업로드 (imageId = null, newFile != null)
 */
@Schema(description = "상품 수정 요청 DTO")
data class UpdateProductRequest(
    @Schema(description = "상품 ID", example = "100", required = true)
    val productId: Long,
    @Schema(description = "사용자 ID", example = "1", required = true)
    val userId: Long,
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
    @Schema(description = "교체할 새 대표 이미지 파일 (없으면 null)", required = false)
    val mainImageFile: MultipartFile? = null,
    @Schema(description = "최종 서브 이미지 목록 (정확히 4개)", required = true)
    val subImagesFinal: List<SubImageFinalRequest> = emptyList(),
    @Schema(description = "최종 추가 이미지 목록 (0~최대 5개)", required = false)
    val additionalImagesFinal: List<AdditionalImageFinalRequest> = emptyList(),
    @Schema(description = "상세 정보", example = "연락처: 010-1234-5678, 상세 문의는 이메일로")
    val detailedInfo: String? = null,
    @Schema(description = "연락처 정보", example = "010-1234-5678")
    val contactInfo: String? = null,
    @Schema(description = "상품 옵션 목록")
    val options: List<UpdateProductOptionRequest> = emptyList(),
) {
    companion object {
        /**
         * 파일 업로드 처리 후, 실제 [Image]가 결정된 상태에서
         * 최종 [Product] 도메인 객체를 만들기 위한 메서드.
         */
        fun toDomain(
            req: UpdateProductRequest,
            mainImage: Image,
            subImages: List<Image>,
            additionalImages: List<Image>,
        ): Product {
            val productTypeEnum = ProductType.valueOf(req.productType)
            val shootingPlaceEnum = ShootingPlace.valueOf(req.shootingPlace)
            val seasonSet = req.availableSeasons.map { ShootingSeason.valueOf(it) }.toSet()
            val cameraSet = req.cameraTypes.map { CameraType.valueOf(it) }.toSet()
            val styleSet = req.retouchStyles.map { RetouchStyle.valueOf(it) }.toSet()

            val domainOptions =
                req.options.map {
                    UpdateProductOptionRequest.toDomain(it, req.productId)
                }

            return Product(
                productId = req.productId,
                userId = req.userId,
                productType = productTypeEnum,
                shootingPlace = shootingPlaceEnum,
                title = req.title,
                description = req.description ?: "",
                availableSeasons = seasonSet,
                cameraTypes = cameraSet,
                retouchStyles = styleSet,
                mainImage = mainImage,
                subImages = subImages,
                additionalImages = additionalImages,
                detailedInfo = req.detailedInfo ?: "",
                contactInfo = req.contactInfo ?: "",
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
 * "최종" 서브 이미지 정보를 담은 DTO
 *
 * - [action] : KEEP / DELETE / UPLOAD
 * - [imageId] : 기존 이미지의 ID (KEEP/DELETE 시 필요)
 * - [newFile] : 새로 업로드할 파일 (UPLOAD 시 필요)
 */
@Schema(description = "최종 서브 이미지 정보 DTO")
data class SubImageFinalRequest(
    @Schema(description = "서브 이미지 처리 액션", example = "KEEP", required = true)
    val action: UpdateSubImageAction,
    @Schema(description = "기존 이미지 ID (KEEP/DELETE 시 필요)", example = "10")
    val imageId: Long?,
    @Schema(description = "새로 업로드할 파일 (UPLOAD 시 필요)")
    val newFile: MultipartFile? = null,
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
 *
 * - [action] : KEEP / DELETE / UPLOAD
 * - [imageId] : 기존 이미지의 ID (KEEP/DELETE 시 필요)
 * - [newFile] : 새로 업로드할 파일 (UPLOAD 시 필요)
 */
@Schema(description = "최종 추가 이미지 정보 DTO")
data class AdditionalImageFinalRequest(
    @Schema(description = "추가 이미지 처리 액션", example = "UPLOAD", required = true)
    val action: UpdateAdditionalImageAction,
    @Schema(description = "기존 이미지 ID (KEEP/DELETE 시 필요)", example = "20")
    val imageId: Long?,
    @Schema(description = "새로 업로드할 파일 (UPLOAD 시 필요)")
    val newFile: MultipartFile? = null,
)

/**
 * [상품 옵션] 수정 요청 DTO
 * - 기존 옵션 업데이트, 신규 옵션 추가, 또는 옵션 삭제 처리를 위한 DTO
 */
@Schema(description = "[상품 옵션] 수정 요청 DTO")
data class UpdateProductOptionRequest(
    @Schema(description = "옵션 ID (옵션 삭제의 경우 null)", example = "0")
    val optionId: Long?,
    @Schema(description = "옵션명", example = "옵션1", required = true)
    val name: String,
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
    @Schema(
        description = "파트너샵 카테고리 (도메인: PartnerShopCategory)",
        example = "HAIR_MAKEUP",
        allowableValues = ["HAIR_MAKEUP", "DRESS", "MENS_SUIT", "BOUQUET", "VIDEO", "STUDIO", "ETC"],
        required = true,
    )
    val category: String,
    @Schema(description = "파트너샵 이름", example = "샘플샵", required = true)
    val name: String,
    @Schema(description = "파트너샵 링크", example = "http://example.com", required = true)
    val link: String,
)
