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
    val partnerShops: List<PartnerShop> = emptyList(),
    val detailedInfo: String? = null,
    val warrantyInfo: String? = null,
    val contactInfo: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val options: List<ProductOption> = emptyList(),
    val images: List<String> = emptyList()
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
        requireNotNull(productId) { "수정 시 상품 ID는 필수입니다" }
    }

    fun updateOptions(newOptions: List<ProductOption>): Pair<List<ProductOption>, Set<Long>> {
        val existingOptions = options.associateBy { it.optionId }
        val newOptionsMap = newOptions.associateBy { it.optionId }

        val toDelete = existingOptions.keys - newOptionsMap.keys
        val toUpdate = newOptions.filter { it.optionId in existingOptions.keys }
        val toInsert = newOptions.filter { it.optionId == null }

        val updatedOptions = (toInsert + toUpdate).map { it.copy(productId = productId) }

        return Pair(updatedOptions, toDelete.filterNotNull().toSet())
    }
}
