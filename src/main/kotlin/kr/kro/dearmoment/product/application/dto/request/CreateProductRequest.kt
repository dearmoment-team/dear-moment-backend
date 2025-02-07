package kr.kro.dearmoment.product.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import kr.kro.dearmoment.product.domain.model.ConceptType
import kr.kro.dearmoment.product.domain.model.OriginalProvideType
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.domain.model.SeasonHalf
import java.time.LocalDateTime

data class CreateProductRequest(
    val userId: Long,
    @field:NotBlank(message = "상품명은 필수입니다.")
    val title: String,
    val description: String?,
    @field:PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    val price: Long,
    /**
     * 0=일반, 1=패키지 등 구분
     */
    val typeCode: Int,
    /**
     * 우아한, 빈티지, 모던, 클래식, 럭셔리 등
     */
    val concept: ConceptType,
    /**
     * 원본 제공 타입
     * - FULL: 원본 전체 제공
     * - PARTIAL: 원본 일부(몇 장만) 제공
     */
    val originalProvideType: OriginalProvideType,
    /**
     * PARTIAL인 경우 제공할 원본 장수 (1 이상)
     * FULL일 경우는 null 또는 0이어야 합니다.
     */
    val partialOriginalCount: Int? = null,
    val shootingTime: LocalDateTime?,
    val shootingLocation: String?,
    /**
     * 최대 의상 벌 수
     */
    val numberOfCostumes: Int?,
    /**
     * 연도 (예: 25년 등)
     */
    val seasonYear: Int?,
    /**
     * 상반기/하반기 여부
     */
    val seasonHalf: SeasonHalf?,
    val partnerShops: List<CreatePartnerShopRequest>,
    val detailedInfo: String?,
    val warrantyInfo: String?,
    val contactInfo: String?,
    val options: List<CreateProductOptionRequest>,
    // images 필드는 제거합니다.
) {
    companion object {
        fun toDomain(request: CreateProductRequest, images: List<String>): Product {
            val partnerShopList =
                request.partnerShops.map { partnerShopRequest ->
                    PartnerShop(
                        name = partnerShopRequest.name,
                        link = partnerShopRequest.link,
                    )
                }
            return Product(
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
                options = emptyList(),
                images = images
            )
        }
    }
}


data class CreatePartnerShopRequest(
    val name: String,
    val link: String,
)

data class CreateProductOptionRequest(
    val optionId: Long? = null,
    @field:NotBlank(message = "옵션 이름은 필수입니다.")
    val name: String,
    @field:PositiveOrZero(message = "추가 가격은 0 이상이어야 합니다.")
    val additionalPrice: Long,
    val description: String? = null,
) {
    companion object {
        fun toDomain(
            request: CreateProductOptionRequest,
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
