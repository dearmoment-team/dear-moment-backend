package kr.kro.dearmoment.product.domain.model.option

/**
 * 패키지 제휴 업체 정보
 */
data class PartnerShop(
    val category: PartnerShopCategory,
    val name: String,
    val link: String,
) {
    init {
        require(name.isNotBlank()) { "제휴 업체 이름은 비어 있을 수 없습니다." }
    }
}
