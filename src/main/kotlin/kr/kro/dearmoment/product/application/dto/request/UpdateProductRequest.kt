package kr.kro.dearmoment.product.application.dto.request

import kr.kro.dearmoment.product.domain.model.*
import kr.kro.dearmoment.image.domain.Image

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
    val mainImage: String,
    val subImages: List<String>,
    val additionalImages: List<String> = emptyList(),
    val detailedInfo: String? = null,
    val contactInfo: String? = null,
    val options: List<UpdateProductOptionRequest> = emptyList(),
) {
    companion object {
        fun toDomain(req: UpdateProductRequest): Product {
            val productTypeEnum = ProductType.valueOf(req.productType)
            val shootingPlaceEnum = ShootingPlace.valueOf(req.shootingPlace)
            val seasonSet = req.availableSeasons.map { ShootingSeason.valueOf(it) }.toSet()
            val cameraSet = req.cameraTypes.map { CameraType.valueOf(it) }.toSet()
            val styleSet = req.retouchStyles.map { RetouchStyle.valueOf(it) }.toSet()

            val mainImg = Image(
                userId = req.userId,
                fileName = req.mainImage,
                url = req.mainImage
            )
            val subImgList = req.subImages.map { Image(userId = req.userId, fileName = it, url = it) }
            val addImgList = req.additionalImages.map { Image(userId = req.userId, fileName = it, url = it) }
            val domainOptions = req.options.map { UpdateProductOptionRequest.toDomain(it, req.productId) }

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
                mainImage = mainImg,
                subImages = subImgList,
                additionalImages = addImgList,
                detailedInfo = req.detailedInfo ?: "",
                contactInfo = req.contactInfo ?: "",
                options = domainOptions
            )
        }
    }
}

data class UpdateProductOptionRequest(
    val optionId: Long?,
    val name: String,
    val optionType: String,
    val discountAvailable: Boolean = false,
    val originalPrice: Long = 0,
    val discountPrice: Long = 0,
    val description: String? = null,
    val costumeCount: Int = 0,
    val shootingLocationCount: Int = 0,
    val shootingHours: Int = 0,
    val shootingMinutes: Int = 0,
    val retouchedCount: Int = 0,
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
                        category = PartnerShopCategory.ETC,
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

data class UpdatePartnerShopRequest(
    val name: String,
    val link: String
)
