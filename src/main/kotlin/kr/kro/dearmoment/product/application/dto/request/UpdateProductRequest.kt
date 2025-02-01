package kr.kro.dearmoment.product.application.dto.request

import java.time.LocalDateTime

data class UpdateProductRequest(
    val userId: Long,
    val productId: Long,
    val title: String,
    val description: String?,
    val price: Long,
    val typeCode: Int,
    val shootingTime: LocalDateTime?,
    val shootingLocation: String?,
    val numberOfCostumes: Int?,
    val partnerShops: List<UpdatePartnerShopRequest>,
    val detailedInfo: String?,
    val warrantyInfo: String?,
    val contactInfo: String?,
    val options: List<UpdateProductOptionRequest>,
    val images: List<String>,
)

data class UpdatePartnerShopRequest(
    val name: String,
    val link: String,
)

data class UpdateProductOptionRequest(
    val optionId: Long?,
    val name: String,
    val additionalPrice: Long,
    val description: String?,
)
