package kr.kro.dearmoment.product.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.*
import org.springframework.web.multipart.MultipartFile

/**
 * [상품 등록] 시 사용하는 요청 DTO
 */
data class CreateProductRequest(

    val userId: Long,

    @field:NotBlank(message = "상품 유형은 필수입니다.")
    val productType: String,

    @field:NotBlank(message = "촬영 장소는 필수입니다.")
    val shootingPlace: String,

    @field:NotBlank(message = "상품 제목은 비어 있을 수 없습니다.")
    val title: String,

    val description: String? = null,

    /**
     * 촬영 가능 시기
     */
    val availableSeasons: List<String> = emptyList(),

    /**
     * 카메라 종류
     */
    val cameraTypes: List<String> = emptyList(),

    /**
     * 보정 스타일
     */
    val retouchStyles: List<String> = emptyList(),

    /**
     * 대표 이미지(1장) - 실제 업로드 파일
     */
    @field:NotNull(message = "대표 이미지는 필수입니다.")
    val mainImageFile: MultipartFile?,

    /**
     * 서브 이미지(필수 4장)
     */
    @field:Size(min = 4, max = 4, message = "서브 이미지는 정확히 4장이어야 합니다.")
    val subImageFiles: List<MultipartFile> = emptyList(),

    /**
     * 추가 이미지(최대 5장)
     */
    @field:Size(max = 5, message = "추가 이미지는 최대 5장까지 등록 가능합니다.")
    val additionalImageFiles: List<MultipartFile> = emptyList(),

    /**
     * 상세 정보, 연락처 등
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
         * toDomain 메서드는 참고 예시입니다.
         * 실제 사용 시, 업로드한 이미지 URL이나 파일명을 어떻게 세팅할지에 따라
         * UseCase/Service 계층에서 처리할 수도 있습니다.
         *
         * 이미지 업로드가 완료되어 URL이 반환된 이후, 해당 URL을 이용하여 Image 객체를 생성합니다.
         * fileName은 URL의 마지막 부분을 추출하여 사용합니다.
         */
        fun toDomain(
            req: CreateProductRequest,
            mainImageUrl: String? = null,
            subImagesUrls: List<String> = emptyList(),
            additionalImagesUrls: List<String> = emptyList(),
        ): Product {
            val productTypeEnum = ProductType.valueOf(req.productType)
            val shootingPlaceEnum = ShootingPlace.valueOf(req.shootingPlace)

            val seasonSet = req.availableSeasons.map { ShootingSeason.valueOf(it) }.toSet()
            val cameraSet = req.cameraTypes.map { CameraType.valueOf(it) }.toSet()
            val styleSet = req.retouchStyles.map { RetouchStyle.valueOf(it) }.toSet()

            val mainImg = mainImageUrl?.let { url ->
                Image(
                    userId = req.userId,
                    fileName = url.substringAfterLast('/'),
                    url = url
                )
            }

            val subImgList = subImagesUrls.map { url ->
                Image(
                    userId = req.userId,
                    fileName = url.substringAfterLast('/'),
                    url = url
                )
            }

            val addImgList = additionalImagesUrls.map { url ->
                Image(
                    userId = req.userId,
                    fileName = url.substringAfterLast('/'),
                    url = url
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
data class CreateProductOptionRequest(

    @field:NotBlank(message = "옵션명은 필수입니다.")
    val name: String,

    @field:NotBlank(message = "옵션 타입은 필수입니다.")
    val optionType: String,

    val discountAvailable: Boolean = false,
    val originalPrice: Long = 0,
    val discountPrice: Long = 0,
    val description: String? = null,

    // 단품용
    val costumeCount: Int = 0,
    val shootingLocationCount: Int = 0,
    val shootingHours: Int = 0,
    val shootingMinutes: Int = 0,
    val retouchedCount: Int = 0,

    val originalProvided: Boolean = false,

    // 패키지용
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
                originalProvided = dto.originalProvided,
                partnerShops = dto.partnerShops.map {
                    PartnerShop(
                        category = PartnerShopCategory.valueOf(it.category),
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
    val category: String,
    val name: String,
    val link: String,
)
