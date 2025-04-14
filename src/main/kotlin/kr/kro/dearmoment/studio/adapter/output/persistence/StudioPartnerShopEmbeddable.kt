package kr.kro.dearmoment.studio.adapter.output.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import kr.kro.dearmoment.product.domain.model.option.PartnerShop
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory

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
        PartnerShop(
            category = PartnerShopCategory.from(category),
            name = name,
            link = urlLink,
        )
}
