package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import kr.kro.dearmoment.product.domain.model.PartnerShopCategory

/**
 * 파트너샵 임베디드
 * - 각 파트너샵이 자체적으로 category/name/link를 가짐
 */
@Embeddable
data class PartnerShopEmbeddable(
    @Enumerated(EnumType.STRING)
    @Column(name = "SHOP_CATEGORY", nullable = false)
    var category: PartnerShopCategory? = null,

    @Column(name = "SHOP_NAME", nullable = false)
    var name: String = "",

    @Column(name = "SHOP_LINK")
    var link: String = "",
)