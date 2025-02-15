package kr.kro.dearmoment.product.application.dto.response

import kr.kro.dearmoment.product.domain.model.ConceptType
import kr.kro.dearmoment.product.domain.model.OriginalProvideType
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.domain.model.SeasonHalf
import java.time.LocalDateTime
import kotlin.time.Duration

data class ProductResponse(
    val productId: Long,
    val userId: Long,
    val title: String,
    val description: String?,
    val price: Long,
    val typeCode: Int,
    val concept: ConceptType,
    val originalProvideType: OriginalProvideType,
    /**
     * PARTIAL인 경우 제공할 원본 장수 (FULL이면 null 또는 0)
     */
    val partialOriginalCount: Int?,
    val shootingTime: Duration?,
    val shootingLocation: String?,
    val numberOfCostumes: Int?,
    val seasonYear: Int?,
    val seasonHalf: SeasonHalf?,
    val partnerShops: List<PartnerShopResponse>,
    val detailedInfo: String?,
    val warrantyInfo: String?,
    val contactInfo: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val options: List<ProductOptionResponse>,
    val images: List<String>,
) {
    companion object {
        fun fromDomain(product: Product): ProductResponse {
            return ProductResponse(
                productId = product.productId,
                userId = product.userId,
                title = product.title,
                description = product.description.takeIf { it.isNotBlank() },
                price = product.price,
                typeCode = product.typeCode,
                concept = product.concept,
                originalProvideType = product.originalProvideType,
                partialOriginalCount = product.partialOriginalCount,
                shootingTime = product.shootingTime,
                shootingLocation = product.shootingLocation.takeIf { it.isNotBlank() },
                numberOfCostumes = product.numberOfCostumes.takeIf { it != 0 },
                seasonYear = product.seasonYear,
                seasonHalf = product.seasonHalf,
                partnerShops = product.partnerShops.map { PartnerShopResponse.fromDomain(it) },
                detailedInfo = product.detailedInfo.takeIf { it.isNotBlank() },
                warrantyInfo = product.warrantyInfo.takeIf { it.isNotBlank() },
                contactInfo = product.contactInfo.takeIf { it.isNotBlank() },
                createdAt = product.createdAt,
                updatedAt = product.updatedAt,
                options = product.options.map { ProductOptionResponse.fromDomain(it) },
                images = product.images.map { it.url },
            )
        }
    }
}

data class PartnerShopResponse(
    val name: String,
    val link: String,
) {
    companion object {
        fun fromDomain(partnerShop: PartnerShop): PartnerShopResponse {
            return PartnerShopResponse(
                name = partnerShop.name,
                link = partnerShop.link,
            )
        }
    }
}

data class ProductOptionResponse(
    val optionId: Long,
    val productId: Long,
    val name: String,
    val additionalPrice: Long,
    val description: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun fromDomain(option: ProductOption): ProductOptionResponse {
            return ProductOptionResponse(
                optionId = option.optionId,
                productId = option.productId,
                name = option.name,
                additionalPrice = option.additionalPrice,
                description = option.description.takeIf { it.isNotBlank() },
                createdAt = option.createdAt,
                updatedAt = option.updatedAt,
            )
        }
    }
}
