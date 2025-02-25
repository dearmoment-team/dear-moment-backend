package kr.kro.dearmoment.product.application.dto.request

import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.*
import org.springframework.web.multipart.MultipartFile

/**
 * [상품 수정] 시 사용하는 요청 DTO
 */
data class UpdateProductRequest(

    val productId: Long,
    val userId: Long,

    val productType: String,
    val shootingPlace: String,
    val title: String,
    val description: String? = null,

    val availableSeasons: List<String> = emptyList(),
    val cameraTypes: List<String> = emptyList(),
    val retouchStyles: List<String> = emptyList(),

    /**
     * 교체할 새 대표 이미지 파일(있을 수도, 없을 수도)
     * null이면 기존 대표 이미지를 그대로 둔다는 의미로 처리할 수 있음
     */
    val mainImageFile: MultipartFile? = null,

    /**
     * 교체할 서브 이미지들(없으면 그대로)
     */
    val subImageFiles: List<MultipartFile>? = null,

    /**
     * 교체할 추가 이미지들(없으면 그대로)
     */
    val additionalImageFiles: List<MultipartFile>? = null,

    val detailedInfo: String? = null,
    val contactInfo: String? = null,

    val options: List<UpdateProductOptionRequest> = emptyList(),
) {
    companion object {
        /**
         * toDomain 역시 등록 로직과 유사하게, 새로 업로드된 이미지 URL들을 받아서
         * Product 도메인에 세팅하는 식으로 구현 가능.
         */
        fun toDomain(
            req: UpdateProductRequest,
            mainImageUrl: String? = null,
            subImagesUrls: List<String> = emptyList(),
            additionalImagesUrls: List<String> = emptyList(),
        ): Product {
            val productTypeEnum = ProductType.valueOf(req.productType)
            val shootingPlaceEnum = ShootingPlace.valueOf(req.shootingPlace)
            val seasonSet = req.availableSeasons.map { ShootingSeason.valueOf(it) }.toSet()
            val cameraSet = req.cameraTypes.map { CameraType.valueOf(it) }.toSet()
            val styleSet = req.retouchStyles.map { RetouchStyle.valueOf(it) }.toSet()

            // 업로드된 새 이미지가 있다면 이미지 객체 생성, 없으면 null
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

            val domainOptions = req.options.map { UpdateProductOptionRequest.toDomain(it, req.productId) }

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
                mainImage = mainImg ?: throw IllegalArgumentException("대표 이미지는 필수입니다."),
                subImages = subImgList,
                additionalImages = addImgList,
                detailedInfo = req.detailedInfo ?: "",
                contactInfo = req.contactInfo ?: "",
                options = domainOptions
            )
        }
    }
}

/**
 * [상품 옵션] 수정 요청 DTO
 */
data class UpdateProductOptionRequest(
    val optionId: Long?,
    val name: String,
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
    val partnerShops: List<UpdatePartnerShopRequest> = emptyList(),
) {
    companion object {
        fun toDomain(dto: UpdateProductOptionRequest, productId: Long): ProductOption {
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
 * [파트너샵] 수정 요청 DTO
 *  - 카테고리를 포함하도록 변경
 */
data class UpdatePartnerShopRequest(
    val category: String,
    val name: String,
    val link: String,
)
