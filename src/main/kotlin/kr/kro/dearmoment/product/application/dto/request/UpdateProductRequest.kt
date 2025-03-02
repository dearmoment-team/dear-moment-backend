package kr.kro.dearmoment.product.application.dto.request

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
 * - 서브이미지는 `subImagesFinal`로 총 4개를 전달받되,
 *   각 항목에 [action], [imageId], [newFile]을 조합하여 최종 상태를 나타냅니다.
 * - 추가이미지는 `additionalImagesFinal`로 최대 5개를 전달받되,
 *   서브이미지와 동일하게 [action], [imageId], [newFile] 조합을 사용합니다.
 *
 * ### action 사용 예시
 * - KEEP: 기존 이미지를 그대로 사용 (imageId != null, newFile = null)
 * - DELETE: 기존 이미지를 삭제 (imageId != null, newFile = null)
 * - UPLOAD: 새 이미지를 업로드 (imageId = null, newFile != null)
 */
data class UpdateProductRequest(
    val productId: Long,
    val userId: Long,
    // 문자열로 들어오는 값들을 enum으로 변환
    val productType: String,
    val shootingPlace: String,
    val title: String,
    val description: String? = null,
    val availableSeasons: List<String> = emptyList(),
    val cameraTypes: List<String> = emptyList(),
    val retouchStyles: List<String> = emptyList(),
    /**
     * 교체할 새 대표 이미지 파일(있을 수도, 없을 수도 있음)
     * null이면 기존 대표 이미지를 그대로 둔다는 의미
     */
    val mainImageFile: MultipartFile? = null,
    /**
     * 최종 서브 이미지(정확히 4개).
     * 각 항목에 [action], [imageId], [newFile]를 통해
     * KEEP / DELETE / UPLOAD를 결정.
     */
    val subImagesFinal: List<SubImageFinalRequest> = emptyList(),
    /**
     * 최종 추가 이미지(0~최대 5개).
     * 서브 이미지와 동일한 방식으로 [action], [imageId], [newFile] 조합.
     */
    val additionalImagesFinal: List<AdditionalImageFinalRequest> = emptyList(),
    val detailedInfo: String? = null,
    val contactInfo: String? = null,
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
 * 서브 이미지 수정 시, 각 항목을 어떻게 처리할지 나타내는 액션
 */
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
data class SubImageFinalRequest(
    val action: UpdateSubImageAction,
    val imageId: Long?,
    val newFile: MultipartFile? = null,
)

/**
 * 추가 이미지 수정 시, 각 항목을 어떻게 처리할지 나타내는 액션
 */
enum class UpdateAdditionalImageAction {
    KEEP,
    DELETE,
    UPLOAD,
}

/**
 * "최종" 추가 이미지 정보를 담은 DTO
 *
 * - [action] : KEEP / DELETE / UPLOAD
 * - [imageId] : 기존 이미지의 ID
 * - [newFile] : 새로 업로드할 파일
 */
data class AdditionalImageFinalRequest(
    val action: UpdateAdditionalImageAction,
    val imageId: Long?,
    val newFile: MultipartFile? = null,
)

/**
 * [상품 옵션] 수정 요청 DTO
 * - 기존 옵션을 업데이트하거나, 새 옵션을 추가하거나, 또는 옵션 삭제를 처리할 때 사용될 수 있음
 */
data class UpdateProductOptionRequest(
    val optionId: Long?,
    val name: String,
    val optionType: String,
    val discountAvailable: Boolean = false,
    val originalPrice: Long = 0,
    val discountPrice: Long = 0,
    val description: String? = null,
    // 단품용 필드
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
data class UpdatePartnerShopRequest(
    val category: String,
    val name: String,
    val link: String,
)
