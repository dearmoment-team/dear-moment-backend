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
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val options: List<ProductOption> = emptyList(),
    val images: List<String>
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

    /**
     * 기존 옵션과 새 옵션 목록을 비교하여 업데이트할 옵션과 삭제할 옵션을 식별한다.
     * 옵션 식별자는 0L이면 신규 옵션으로 간주한다.
     */
    fun updateOptions(newOptions: List<ProductOption>): ProductOptionUpdateResult {
        val existingOptionsMap = options.associateBy { it.optionId }
        val newOptionsMap = newOptions.associateBy { it.optionId }

        // 기존에 존재하는 옵션 중 새 옵션 목록에 포함되지 않은 옵션은 삭제 대상
        val toDelete: Set<Long> = (existingOptionsMap.keys - newOptionsMap.keys).filterNotNull().toSet()

        // 업데이트할 옵션: optionId가 0L(신규가 아님)이고 기존 옵션에 해당하는 경우
        val toUpdate = newOptions.filter { it.optionId != 0L && existingOptionsMap.containsKey(it.optionId) }
            .map { it.copy(productId = productId) }

        // 신규 옵션: optionId가 0L인 경우
        val toInsert = newOptions.filter { it.optionId == 0L }
            .map { it.copy(productId = productId) }

        // 업데이트된 옵션을 먼저, 신규 옵션을 나중에 추가
        val updatedOptions = toUpdate + toInsert

        return ProductOptionUpdateResult(updatedOptions, toDelete)
    }
}
