package kr.kro.dearmoment.product.application.dto.response

import kr.kro.dearmoment.product.domain.model.*
import java.time.LocalDateTime

data class ProductResponse(
    val productId: Long,
    val userId: Long,
    val productType: String,
    val shootingPlace: String,
    val title: String,
    val description: String?,
    val availableSeasons: List<String>,
    val cameraTypes: List<String>,
    val retouchStyles: List<String>,

    val mainImage: String,
    val subImages: List<String>,
    val additionalImages: List<String>,

    val detailedInfo: String?,
    val contactInfo: String?,

    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,

    val options: List<ProductOptionResponse>,
) {
    companion object {
        fun fromDomain(prod: Product): ProductResponse {
            return ProductResponse(
                productId = prod.productId,
                userId = prod.userId,
                productType = prod.productType.name,
                shootingPlace = prod.shootingPlace.name,
                title = prod.title,
                description = prod.description.takeIf { it.isNotBlank() },

                availableSeasons = prod.availableSeasons.map { it.name },
                cameraTypes = prod.cameraTypes.map { it.name },
                retouchStyles = prod.retouchStyles.map { it.name },

                mainImage = prod.mainImage.url,
                subImages = prod.subImages.map { it.url },
                additionalImages = prod.additionalImages.map { it.url },

                detailedInfo = prod.detailedInfo.takeIf { it.isNotBlank() },
                contactInfo = prod.contactInfo.takeIf { it.isNotBlank() },

                createdAt = prod.createdAt,
                updatedAt = prod.updatedAt,

                options = prod.options.map { ProductOptionResponse.fromDomain(it) },
            )
        }
    }
}

data class ProductOptionResponse(
    val optionId: Long,
    val productId: Long,
    val name: String,
    val optionType: String,
    val discountAvailable: Boolean,
    val originalPrice: Long,
    val discountPrice: Long,
    val description: String?,
    val costumeCount: Int,
    val shootingLocationCount: Int,
    val shootingHours: Int,
    val shootingMinutes: Int,
    val retouchedCount: Int,

    val partnerShops: List<PartnerShopResponse>,

    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun fromDomain(opt: ProductOption): ProductOptionResponse {
            return ProductOptionResponse(
                optionId = opt.optionId,
                productId = opt.productId,
                name = opt.name,
                optionType = opt.optionType.name,
                discountAvailable = opt.discountAvailable,
                originalPrice = opt.originalPrice,
                discountPrice = opt.discountPrice,
                description = opt.description.takeIf { it.isNotBlank() },

                costumeCount = opt.costumeCount,
                shootingLocationCount = opt.shootingLocationCount,
                shootingHours = opt.shootingHours,
                shootingMinutes = opt.shootingMinutes,
                retouchedCount = opt.retouchedCount,

                partnerShops = opt.partnerShops.map { PartnerShopResponse.fromDomain(it) },

                createdAt = opt.createdAt,
                updatedAt = opt.updatedAt
            )
        }
    }
}

data class PartnerShopResponse(
    val category: String,
    val name: String,
    val link: String
) {
    companion object {
        fun fromDomain(ps: PartnerShop): PartnerShopResponse {
            return PartnerShopResponse(
                category = ps.category.name,
                name = ps.name,
                link = ps.link,
            )
        }
    }
}
