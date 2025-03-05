package kr.kro.dearmoment.studio.application.command

import kr.kro.dearmoment.studio.domain.StudioPartnerShop
import kr.kro.dearmoment.studio.domain.StudioPartnerShopCategory

data class StudioPartnerShopCommand(
    val category: String,
    val name: String,
    val urlLink: String,
) {
    fun toDomain() =
        StudioPartnerShop(
            category = StudioPartnerShopCategory.from(category),
            name = name,
            urlLink = urlLink,
        )
}
