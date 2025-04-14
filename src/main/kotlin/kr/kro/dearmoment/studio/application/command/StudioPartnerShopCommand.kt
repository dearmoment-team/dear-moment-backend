package kr.kro.dearmoment.studio.application.command

import kr.kro.dearmoment.product.domain.model.option.PartnerShop
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory

data class StudioPartnerShopCommand(
    val category: String,
    val name: String,
    val urlLink: String,
) {
    fun toDomain() =
        PartnerShop(
            category = PartnerShopCategory.from(category),
            name = name,
            link = urlLink,
        )
}
