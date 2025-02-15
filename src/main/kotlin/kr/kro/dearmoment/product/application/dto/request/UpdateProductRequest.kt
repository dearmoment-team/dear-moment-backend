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

// 이미지 식별 정보를 나타내는 값 객체입니다.
// 내부적으로는 고유 fileName이나 플레이스홀더("new_0", "new_1", …) 값을 담습니다.
data class ImageReference(
    val identifier: String,
) {
    override fun toString(): String = identifier
}

// 프론트엔드에서는 업데이트 시 이미지 관련 정보를 ImageReference 타입의 리스트로 전달합니다.
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
    // shootingTime: 기존 LocalDateTime?에서 Duration? 타입으로 변경
    val shootingTime: Duration?,
    val shootingLocation: String?,
    val numberOfCostumes: Int?,
    val seasonYear: Int?,
    val seasonHalf: SeasonHalf?,
    val partnerShops: List<UpdatePartnerShopRequest>,
    val detailedInfo: String?,
    val warrantyInfo: String?,
    val contactInfo: String?,
    val options: List<UpdateProductOptionRequest>,
    // 업데이트 시 프론트엔드에서 이미지 식별자들을 전달합니다.
    // ex) 기존 이미지의 고유 fileName 또는 신규 이미지의 플레이스홀더("new_0", "new_1", …)
    val images: List<ImageReference>,
) {
    companion object {
        // 백엔드 내부에서 최종 Image 도메인 객체 리스트가 구성되면 이를 Product 도메인 모델에 주입합니다.
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
                shootingTime = request.shootingTime,
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
