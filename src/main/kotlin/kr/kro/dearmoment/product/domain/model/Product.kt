package kr.kro.dearmoment.product.domain.model

import java.time.LocalDateTime

data class Product(
    val productId: Long = 0L,
    val userId: Long,
    val title: String,
    val description: String = "",
    val price: Long,
    val typeCode: Int,
    val shootingTime: LocalDateTime? = null,
    val shootingLocation: String = "",
    val numberOfCostumes: Int = 0,
    val partnerShops: List<PartnerShop> = emptyList(),
    val detailedInfo: String = "",
    val warrantyInfo: String = "",
    val contactInfo: String = "",
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val options: List<ProductOption> = emptyList(),
    val images: List<String>,
) {
    init {
        require(images.isNotEmpty()) { "최소 1개 이상의 이미지가 필요합니다" }
        require(title.isNotBlank()) { "상품명은 필수 입력값입니다" }
        require(price >= 0) { "가격은 0 이상이어야 합니다" }

        if (hasPackage) {
            require(partnerShops.isNotEmpty()) {
                "패키지 상품은 하나 이상의 협력업체 정보가 필요합니다."
            }
            partnerShops.forEach {
                require(it.name.isNotBlank()) { "파트너샵 이름은 비어 있을 수 없습니다." }
                require(it.link.isNotBlank()) { "파트너샵 링크는 비어 있을 수 없습니다." }
            }
        }
    }

    val hasPackage: Boolean
        get() = typeCode == 1

    fun validateForUpdate() {
        require(productId != 0L) { "수정 시 상품 ID는 필수입니다" }
    }

    fun updateOptions(newOptions: List<ProductOption>): ProductOptionUpdateResult {
        val existingOptionsMap = options.associateBy { it.optionId }
        val newOptionsMap = newOptions.associateBy { it.optionId }

        val toDelete: Set<Long> =
            (existingOptionsMap.keys - newOptionsMap.keys)
                .filterNotNull()
                .toSet()

        val toUpdate =
            newOptions.filter {
                it.optionId != 0L && existingOptionsMap.containsKey(it.optionId)
            }.map { it.copy(productId = productId) }

        val toInsert =
            newOptions.filter {
                it.optionId == 0L
            }.map { it.copy(productId = productId) }

        val updatedOptions = toUpdate + toInsert
        return ProductOptionUpdateResult(updatedOptions, toDelete)
    }
}
