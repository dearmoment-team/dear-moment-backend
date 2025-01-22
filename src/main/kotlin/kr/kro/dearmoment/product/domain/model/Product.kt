package kr.kro.dearmoment.product.domain.model

import java.time.LocalDateTime

data class Product(
    val productId: Long,
    val userId: Long? = null,
    val title: String,
    val description: String? = null,
    val price: Long,
    val typeCode: Int,
    val shootingTime: LocalDateTime? = null,
    val shootingLocation: String? = null,
    val numberOfCostumes: Int? = null,
    val packagePartnerShops: String? = null,
    val detailedInfo: String? = null,
    val warrantyInfo: String? = null,
    val contactInfo: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val options: List<ProductOption> = emptyList(),
) {
    val hasPackage: Boolean
        get() = (typeCode == 1)
}
