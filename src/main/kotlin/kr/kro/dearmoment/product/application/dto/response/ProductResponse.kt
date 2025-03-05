package kr.kro.dearmoment.product.application.dto.response

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

    fun toDomain(): Product {
        return Product(
            productId = this.productId,
            userId = this.userId,
            productType = ProductType.valueOf(this.productType),
            shootingPlace = ShootingPlace.valueOf(this.shootingPlace),
            title = this.title,
            description = this.description ?: "",
            availableSeasons = this.availableSeasons.map { ShootingSeason.valueOf(it) }.toSet(),
            cameraTypes = this.cameraTypes.map { CameraType.valueOf(it) }.toSet(),
            retouchStyles = this.retouchStyles.map { RetouchStyle.valueOf(it) }.toSet(),
            mainImage =
                Image(
                    userId = this.userId,
                    fileName = this.mainImage.substringAfterLast('/'),
                    url = this.mainImage,
                ),
            subImages =
                this.subImages.map { url ->
                    Image(
                        userId = this.userId,
                        fileName = url.substringAfterLast('/'),
                        url = url,
                    )
                },
            additionalImages =
                this.additionalImages.map { url ->
                    Image(
                        userId = this.userId,
                        fileName = url.substringAfterLast('/'),
                        url = url,
                    )
                },
            detailedInfo = this.detailedInfo ?: "",
            contactInfo = this.contactInfo ?: "",
            createdAt = this.createdAt ?: LocalDateTime.now(),
            updatedAt = this.updatedAt ?: LocalDateTime.now(),
            options = this.options.map { it.toDomain() },
        )
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
    val updatedAt: LocalDateTime?,
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
                updatedAt = opt.updatedAt,
            )
        }
    }

    fun toDomain(): ProductOption {
        return ProductOption(
            optionId = this.optionId,
            productId = this.productId,
            name = this.name,
            optionType = OptionType.valueOf(this.optionType),
            discountAvailable = this.discountAvailable,
            originalPrice = this.originalPrice,
            discountPrice = this.discountPrice,
            description = this.description ?: "",
            costumeCount = this.costumeCount,
            shootingLocationCount = this.shootingLocationCount,
            shootingHours = this.shootingHours,
            shootingMinutes = this.shootingMinutes,
            retouchedCount = this.retouchedCount,
            originalProvided = true,
            partnerShops = this.partnerShops.map { it.toDomain() },
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
    }
}

data class PartnerShopResponse(
    val category: String,
    val name: String,
    val link: String,
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

    fun toDomain(): PartnerShop {
        return PartnerShop(
            category = PartnerShopCategory.valueOf(this.category),
            name = this.name,
            link = this.link,
        )
    }
}
