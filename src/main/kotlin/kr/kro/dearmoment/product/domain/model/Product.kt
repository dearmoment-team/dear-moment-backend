package kr.kro.dearmoment.product.domain.model

import java.time.LocalDateTime

data class Product(
    val productId: Long? = null,
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
    val images: List<String> = emptyList()  // 이미지 URL 리스트 추가
) {
    val hasPackage: Boolean
        get() = typeCode == 1
}
