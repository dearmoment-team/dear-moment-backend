package kr.kro.dearmoment.studio.adapter.output.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import kr.kro.dearmoment.studio.domain.StudioPartnerShop
import kr.kro.dearmoment.studio.domain.StudioPartnerShopCategory

@Embeddable
class StudioPartnerShopEmbeddable(
    @Column(nullable = false)
    val category: String,
    @Column(nullable = false)
    val name: String,
    @Column()
    val urlLink: String,
) {
    fun toDomain() =
        StudioPartnerShop(
            category = StudioPartnerShopCategory.from(category),
            name = name,
            urlLink = urlLink,
        )
}
