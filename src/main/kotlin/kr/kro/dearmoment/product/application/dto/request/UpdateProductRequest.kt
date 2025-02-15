package kr.kro.dearmoment.product.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.ConceptType
import kr.kro.dearmoment.product.domain.model.OriginalProvideType
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.domain.model.SeasonHalf
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class ImageReference(
    val identifier: String,
) {
    override fun toString(): String = identifier
}

/**
 * [UpdateProductRequest]
 * - 프론트엔드에서 30, 60 등의 "분 단위"로 촬영시간을 보냄
 * - shootingTimeMinutes 필드 사용
 * - toDomain에서 shootingTimeMinutes를 Duration으로 변환
 */
data class UpdateProductRequest(
    val productId: Long,
    val userId: Long,
    @field:NotBlank(message = "상품명은 필수입니다.")
    val title: String,
    val description: String?,
    @field:PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    val price: Long,
    val typeCode: Int,
    val concept: ConceptType,
    val originalProvideType: OriginalProvideType,
    val partialOriginalCount: Int? = null,
    /**
     * shootingTimeMinutes(분 단위)
     */
    @field:PositiveOrZero(message = "촬영 시간은 0분 이상이어야 합니다.")
    val shootingTimeMinutes: Int?,
    val shootingLocation: String?,
    val numberOfCostumes: Int?,
    val seasonYear: Int?,
    val seasonHalf: SeasonHalf?,
    val partnerShops: List<UpdatePartnerShopRequest>,
    val detailedInfo: String?,
    val warrantyInfo: String?,
    val contactInfo: String?,
    val options: List<UpdateProductOptionRequest>,
    // 이미지 식별자 리스트 (기존 이미지 파일명, 새 파일은 "new_x" 등)
    val images: List<ImageReference>,
) {
    companion object {
        fun toDomain(
            request: UpdateProductRequest,
            images: List<Image>,
        ): Product {
            val partnerShopList = request.partnerShops.map { partnerShopRequest ->
                PartnerShop(
                    name = partnerShopRequest.name,
                    link = partnerShopRequest.link,
                )
            }
            val productOptionList = request.options.map { optionRequest ->
                UpdateProductOptionRequest.toDomain(optionRequest, request.productId)
            }

            // shootingTimeMinutes -> Duration 변환
            val duration: Duration? = request.shootingTimeMinutes
                ?.takeIf { it > 0 }
                ?.toDuration(DurationUnit.MINUTES)

            return Product(
                productId = request.productId,
                userId = request.userId,
                title = request.title,
                description = request.description ?: "",
                price = request.price,
                typeCode = request.typeCode,
                concept = request.concept,
                originalProvideType = request.originalProvideType,
                partialOriginalCount = request.partialOriginalCount,
                shootingTime = duration,
                shootingLocation = request.shootingLocation ?: "",
                numberOfCostumes = request.numberOfCostumes ?: 0,
                seasonYear = request.seasonYear,
                seasonHalf = request.seasonHalf,
                partnerShops = partnerShopList,
                detailedInfo = request.detailedInfo ?: "",
                warrantyInfo = request.warrantyInfo ?: "",
                contactInfo = request.contactInfo ?: "",
                options = productOptionList,
                images = images,
            )
        }
    }
}

data class UpdatePartnerShopRequest(
    val name: String,
    val link: String,
) {
    fun toDomain(): PartnerShop {
        return PartnerShop(name, link)
    }
}

data class UpdateProductOptionRequest(
    val optionId: Long?,
    @field:NotBlank(message = "옵션 이름은 필수입니다.")
    val name: String,
    @field:PositiveOrZero(message = "추가 가격은 0 이상이어야 합니다.")
    val additionalPrice: Long,
    val description: String?,
) {
    companion object {
        fun toDomain(
            request: UpdateProductOptionRequest,
            productId: Long,
        ): ProductOption {
            return ProductOption(
                optionId = request.optionId ?: 0L,
                productId = productId,
                name = request.name,
                additionalPrice = request.additionalPrice,
                description = request.description ?: "",
            )
        }
    }
}
