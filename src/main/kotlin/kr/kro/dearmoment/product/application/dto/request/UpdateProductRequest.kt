package kr.kro.dearmoment.product.application.dto.request

import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.*
import org.springframework.web.multipart.MultipartFile

/**
 * [상품 수정] 시 사용하는 요청 DTO
 *
 * - 서브이미지는 `subImagesFinal`로 4개를 넘겨받고,
 *   각 항목에 imageId(기존 이미지 ID)와 file(새 이미지 파일) 조합을 넣어주면 됩니다.
 * - 추가이미지는 `additionalImagesFinal`로 최대 5개를 넘겨받습니다.
 * - 기존 이미지 유지: imageId != null, file = null
 * - 기존 이미지 교체: imageId != null, file != null
 * - 새 이미지 추가: imageId = null, file != null
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
     * null이면 기존 대표 이미지를 그대로 둔다는 의미
     */
    val mainImageFile: MultipartFile? = null,

    /**
     * 최종 서브이미지 (정확히 4개)
     * 기존 이미지를 계속 쓰려면 imageId만 주고 file은 null
     * 교체하려면 imageId와 file 모두 넣기
     * 새로 추가하려면 imageId=null, file!=null
     */
    val subImagesFinal: List<SubImageFinalRequest> = emptyList(),

    /**
     * 최종 추가이미지 (0~최대 5개)
     * 위와 동일한 방식으로 imageId, file 조합을 전달
     */
    val additionalImagesFinal: List<AdditionalImageFinalRequest> = emptyList(),

    val detailedInfo: String? = null,
    val contactInfo: String? = null,

    val options: List<UpdateProductOptionRequest> = emptyList(),
) {
    /**
     * toDomain()은 업로드된 이미지들의 URL 등을 외부에서 주입받아
     * 최종 Product 도메인 객체를 생성할 때 사용합니다.
     *
     * - mainImageUrl: 새로 업로드된 대표이미지의 최종 URL
     * - subImagesUrls: 최종 서브이미지 4장의 URL
     * - additionalImagesUrls: 최종 추가이미지의 URL
     */
    companion object {
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

            val domainOptions = req.options.map {
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
                options = domainOptions
            )
        }
    }
}

/**
 * "최종" 서브 이미지 정보
 * - imageId: 기존 이미지가 있으면 그 ID
 * - file: 새 파일
 */
data class SubImageFinalRequest(
    val imageId: Long?,              // 기존 이미지면 ID, 신규 추가면 null
    val file: MultipartFile? = null, // 새 파일
)

/**
 * "최종" 추가 이미지 정보
 */
data class AdditionalImageFinalRequest(
    val imageId: Long?,              // 기존 이미지면 ID, 신규 추가면 null
    val file: MultipartFile? = null, // 새 파일
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
