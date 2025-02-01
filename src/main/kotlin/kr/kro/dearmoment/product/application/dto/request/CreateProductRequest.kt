package kr.kro.dearmoment.product.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import java.time.LocalDateTime

data class CreateProductRequest(
    val userId: Long,
    val title: String,
    val description: String?,
    val price: Long,
    val typeCode: Int,
    val shootingTime: LocalDateTime?,
    val shootingLocation: String?,
    val numberOfCostumes: Int?,
    val partnerShops: List<CreatePartnerShopRequest>,
    val detailedInfo: String?,
    val warrantyInfo: String?,
    val contactInfo: String?,
    val options: List<CreateProductOptionRequest>,
    val images: List<String>,
) {
    companion object {
        fun toDomain(request: CreateProductRequest): kr.kro.dearmoment.product.domain.model.Product {
            val partnerShopList = request.partnerShops.map { partnerShopRequest ->
                kr.kro.dearmoment.product.domain.model.PartnerShop(
                    name = partnerShopRequest.name,
                    link = partnerShopRequest.link
                )
            }
            return kr.kro.dearmoment.product.domain.model.Product(
                userId = request.userId,
                title = request.title,
                description = request.description ?: "",
                price = request.price,
                typeCode = request.typeCode,
                shootingTime = request.shootingTime,
                shootingLocation = request.shootingLocation ?: "",
                numberOfCostumes = request.numberOfCostumes ?: 0,
                partnerShops = partnerShopList,
                detailedInfo = request.detailedInfo ?: "",
                warrantyInfo = request.warrantyInfo ?: "",
                contactInfo = request.contactInfo ?: "",
                images = request.images,
                options = emptyList()
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
        fun toDomain(request: CreateProductOptionRequest, productId: Long): kr.kro.dearmoment.product.domain.model.ProductOption {
            return kr.kro.dearmoment.product.domain.model.ProductOption(
                optionId = request.optionId ?: 0L,
                productId = productId,
                name = request.name,
                additionalPrice = request.additionalPrice,
                description = request.description ?: ""
            )
        }
    }
}
