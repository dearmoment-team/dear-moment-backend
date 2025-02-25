package kr.kro.dearmoment.product.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.*
import org.springframework.web.multipart.MultipartFile

/**
 * [상품 등록] 시 사용하는 요청 DTO
 */
data class CreateProductRequest(

    val userId: Long,

    @field:NotBlank
    val productType: String,

    @field:NotBlank
    val shootingPlace: String,

    @field:NotBlank
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
         * 이하 toDomain은 (참고용) 예시 로직:
         * 실제 업로드된 이미지(파일)는 ImageService를 통해 업로드 후,
         * 그 결과(이미지 URL, 파일명 등)를 Product 도메인에 넣는 과정을
         * UseCase/Service에서 처리하게 될 가능성이 큼.
         *
         * 만약 여기서 단순 문자열만 받아 도메인으로 변환하던 기존 로직을 남겨두시려면,
         * 'req.mainImageFile', 'req.subImageFiles' 등은 별도 처리가 필요합니다.
         * 아래 예시는 "이미지 업로드 후 URL을 다시 세팅"한다는 가정하에
         * 임시로 Image 도메인 객체를 생성해준다고 가정한 구조입니다.
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
                    fileName = url,
                    url = url
                )
            }

            val subImgList = subImagesUrls.map { url ->
                Image(
                    userId = req.userId,
                    fileName = url,
                    url = url
                )
            }

            val addImgList = additionalImagesUrls.map { url ->
                Image(
                    userId = req.userId,
                    fileName = url,
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
    val name: String,

    @field:NotBlank
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
                partnerShops = dto.partnerShops.map {
                    // 파트너샵 카테고리를 요청 DTO에서 받아서 사용
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
