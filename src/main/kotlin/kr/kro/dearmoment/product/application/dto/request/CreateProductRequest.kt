package kr.kro.dearmoment.product.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.*
import java.time.LocalDateTime

/**
 * [상품 등록] 시 사용하는 요청 DTO
 */
data class CreateProductRequest(

    val userId: Long,

    /**
     * 상품 유형 (예: WEDDING_SNAP)
     */
    @field:NotBlank
    val productType: String,

    /**
     * 촬영 장소 (예: JEJU)
     */
    @field:NotBlank
    val shootingPlace: String,

    /**
     * 상품명
     */
    @field:NotBlank
    val title: String,

    /**
     * 간단 설명(선택)
     */
    val description: String? = null,

    /**
     * 촬영 가능 시기 (예: [ "YEAR_2025_FIRST_HALF", "YEAR_2025_SECOND_HALF" ])
     */
    val availableSeasons: List<String> = emptyList(),

    /**
     * 카메라 종류 (예: ["DIGITAL", "FILM"])
     */
    val cameraTypes: List<String> = emptyList(),

    /**
     * 보정 스타일 (예: ["MODERN", "CHIC"], 최대 2개)
     */
    val retouchStyles: List<String> = emptyList(),

    /**
     * 대표 이미지 (파일명/URL)
     */
    @field:NotBlank
    val mainImage: String,

    /**
     * 서브 이미지(필수 4장)
     */
    @field:Size(min = 4, max = 4, message = "서브 이미지는 정확히 4장이어야 합니다.")
    val subImages: List<String>,

    /**
     * 추가 이미지(최대 5장)
     */
    @field:Size(max = 5, message = "추가 이미지는 최대 5장까지 등록 가능합니다.")
    val additionalImages: List<String> = emptyList(),

    /**
     * 상세 정보, 연락처 정보 등
     */
    val detailedInfo: String? = null,
    val contactInfo: String? = null,

    /**
     * 옵션 목록
     */
    val options: List<CreateProductOptionRequest> = emptyList(),
) {
    companion object {
        /**
         * DTO -> 도메인 변환
         */
        fun toDomain(req: CreateProductRequest): Product {
            val productTypeEnum: ProductType = ProductType.valueOf(req.productType)
            val shootingPlaceEnum: ShootingPlace = ShootingPlace.valueOf(req.shootingPlace)

            val seasonSet: Set<ShootingSeason> = req.availableSeasons
                .map { ShootingSeason.valueOf(it) }
                .toSet()

            val cameraSet: Set<CameraType> = req.cameraTypes
                .map { CameraType.valueOf(it) }
                .toSet()

            val styleSet: Set<RetouchStyle> = req.retouchStyles
                .map { RetouchStyle.valueOf(it) }
                .toSet()

            // 대표 이미지 (도메인의 Image 객체로)
            val mainImg = Image(
                userId = req.userId,
                fileName = req.mainImage,
                url = req.mainImage,
            )

            // 서브 이미지
            val subImgList = req.subImages.map {
                Image(
                    userId = req.userId,
                    fileName = it,
                    url = it
                )
            }

            // 추가 이미지
            val addImgList = req.additionalImages.map {
                Image(
                    userId = req.userId,
                    fileName = it,
                    url = it
                )
            }

            // 옵션 목록
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
                mainImage = mainImg,
                subImages = subImgList,
                additionalImages = addImgList,
                detailedInfo = req.detailedInfo ?: "",
                contactInfo = req.contactInfo ?: "",
                // createdAt, updatedAt는 엔티티의 Auditing에 의해 자동 관리됩니다.
                options = domainOptions,
            )
        }
    }
}

/**
 * [상품 옵션] 생성 요청 DTO
 */
data class CreateProductOptionRequest(
    val name: String,

    /**
     * 옵션 타입 (SINGLE / PACKAGE)
     */
    @field:NotBlank
    val optionType: String,

    /**
     * 할인 여부
     */
    val discountAvailable: Boolean = false,

    /**
     * 원 판매가
     */
    val originalPrice: Long = 0,

    /**
     * 할인가
     */
    val discountPrice: Long = 0,

    /**
     * 옵션 설명
     */
    val description: String? = null,

    /**
     * [단품용] 의상 수
     */
    val costumeCount: Int = 0,

    /**
     * [단품용] 촬영 장소 수
     */
    val shootingLocationCount: Int = 0,

    /**
     * [단품용] 촬영 시간(시/분)
     */
    val shootingHours: Int = 0,
    val shootingMinutes: Int = 0,

    /**
     * [단품용] 보정본
     */
    val retouchedCount: Int = 0,

    /**
     * [패키지용] 파트너샵 리스트
     */
    val partnerShops: List<CreatePartnerShopRequest> = emptyList(),
) {
    companion object {
        fun toDomain(dto: CreateProductOptionRequest, productId: Long): ProductOption {
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
                partnerShops = dto.partnerShops.map {
                    PartnerShop(
                        category = PartnerShopCategory.ETC,
                        name = it.name,
                        link = it.link
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
data class CreatePartnerShopRequest(
    val name: String,
    val link: String,
)
