package kr.kro.dearmoment.product.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import kr.kro.dearmoment.product.domain.model.ConceptType
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
     * 원본 제공 여부
     */
    val provideOriginal: Boolean,
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
    val images: List<String>,
) {
    companion object {
        fun toDomain(request: CreateProductRequest): Product {
            val partnerShopList = request.partnerShops.map { partnerShopRequest ->
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
                provideOriginal = request.provideOriginal,
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
                images = request.images,
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
