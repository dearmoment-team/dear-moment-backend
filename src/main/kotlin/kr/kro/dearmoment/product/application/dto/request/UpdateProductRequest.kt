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
     * null이면 기존 대표 이미지를 그대로 둔다는 의미로 처리
     */
    val mainImageFile: MultipartFile? = null,

    /**
     * 전체 서브 이미지를 교체할 경우 사용 (새 파일 목록)
     * null이면 기존 서브 이미지를 유지
     */
    val subImageFiles: List<MultipartFile>? = null,

    /**
     * 부분 업데이트가 필요한 경우, 서브 이미지별 업데이트 정보를 전달
     * 각 항목은 기존 이미지의 삭제, 수정 또는 신규 추가를 나타냅니다.
     */
    val subImageUpdates: List<SubImageUpdateRequest> = emptyList(),

    /**
     * 전체 추가 이미지를 교체할 경우 사용 (새 파일 목록)
     * null이면 기존 추가 이미지를 유지
     */
    val additionalImageFiles: List<MultipartFile>? = null,

    /**
     * 부분 업데이트가 필요한 경우, 추가 이미지별 업데이트 정보를 전달
     * (프로젝트 요구사항에 따라 추가 가능)
     */
    val additionalImageUpdates: List<SubImageUpdateRequest> = emptyList(),

    val detailedInfo: String? = null,
    val contactInfo: String? = null,

    val options: List<UpdateProductOptionRequest> = emptyList(),
) {
    companion object {
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
 * [서브/추가 이미지 업데이트] 요청 DTO
 * - 기존 이미지의 삭제, 교체, 신규 추가를 위한 정보를 포함합니다.
 */
data class SubImageUpdateRequest(
    /**
     * 기존 이미지의 식별자. null이면 신규 이미지 추가로 간주
     */
    val imageId: Long? = null,

    /**
     * 새로운 이미지 파일. 이 값이 존재하면 해당 파일로 업데이트
     */
    val newImageFile: MultipartFile? = null,

    /**
     * true이면 해당 이미지를 삭제 처리
     */
    val isDeleted: Boolean = false,
)

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

    val originalProvided: Boolean = false,

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
 */
data class UpdatePartnerShopRequest(
    val category: String,
    val name: String,
    val link: String,
)
