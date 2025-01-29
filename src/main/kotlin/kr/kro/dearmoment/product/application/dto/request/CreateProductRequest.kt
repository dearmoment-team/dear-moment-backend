package kr.kro.dearmoment.product.application.dto.request

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
)

data class CreatePartnerShopRequest(
    val name: String,
    val link: String,
)

data class CreateProductOptionRequest(
    val name: String,
    val additionalPrice: Long,
    val description: String? = null,
)
