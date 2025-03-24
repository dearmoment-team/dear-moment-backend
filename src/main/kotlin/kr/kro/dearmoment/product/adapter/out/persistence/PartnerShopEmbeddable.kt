package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * 파트너샵 임베디드
 * - 각 파트너샵이 자체적으로 category/name/link를 가짐
 */
@Embeddable
data class PartnerShopEmbeddable(
    @Column(name = "SHOP_CATEGORY", nullable = false)
    var category: String? = null,
    @Column(name = "SHOP_NAME", nullable = false)
    var name: String = "",
    @Column(name = "SHOP_LINK")
    var link: String = "",
)
