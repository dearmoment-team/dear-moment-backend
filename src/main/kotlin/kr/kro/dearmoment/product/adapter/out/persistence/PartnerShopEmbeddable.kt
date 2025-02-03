package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class PartnerShopEmbeddable(
    @Column(name = "name", nullable = false)
    val name: String = "",
    @Column(name = "link", nullable = false)
    val link: String = "",
)
