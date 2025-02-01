package kr.kro.dearmoment.product.application.dto.response

import java.time.LocalDateTime

data class ProductResponse(
    val productId: Long,
    val userId: Long,
    val title: String,
    val description: String?,
    val price: Long,
    val typeCode: Int,
    val shootingTime: LocalDateTime?,
    val shootingLocation: String?,
    val numberOfCostumes: Int?,
    val partnerShops: List<PartnerShopResponse>,
    val detailedInfo: String?,
    val warrantyInfo: String?,
    val contactInfo: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val options: List<ProductOptionResponse>,
    val images: List<String>,
)

data class PartnerShopResponse(
    val name: String,
    val link: String,
)

data class ProductOptionResponse(
    val optionId: Long,
    val productId: Long,
    val name: String,
    val additionalPrice: Long,
    val description: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
)
