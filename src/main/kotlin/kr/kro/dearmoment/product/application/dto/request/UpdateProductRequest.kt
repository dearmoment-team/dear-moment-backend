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
    val shootingTime: LocalDateTime?,
    val shootingLocation: String?,
    val numberOfCostumes: Int?,
    val seasonYear: Int?,
    val seasonHalf: SeasonHalf?,
    val partnerShops: List<UpdatePartnerShopRequest>,
    val detailedInfo: String?,
    val warrantyInfo: String?,
    val contactInfo: String?,
    val options: List<UpdateProductOptionRequest>,
    val images: List<String>,
) {
    companion object {
        fun toDomain(
            request: UpdateProductRequest,
            images: List<String>,
        ): Product {
            val partnerShopList =
                request.partnerShops.map { partnerShopRequest ->
                    kr.kro.dearmoment.product.domain.model.PartnerShop(
                        name = partnerShopRequest.name,
                        link = partnerShopRequest.link,
                    )
                }
            val productOptionList =
                request.options.map { optionRequest ->
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
