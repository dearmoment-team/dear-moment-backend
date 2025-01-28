package kr.kro.dearmoment.product.domain.model

data class PartnerShop(
    val name: String,
    val link: String
) {
    init {
        require(name.isNotBlank()) { "파트너샵 이름은 비어 있을 수 없습니다." }
        require(link.isNotBlank()) { "파트너샵 링크는 비어 있을 수 없습니다." }
    }
}