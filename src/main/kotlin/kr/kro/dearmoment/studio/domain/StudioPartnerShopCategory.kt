package kr.kro.dearmoment.studio.domain

enum class StudioPartnerShopCategory {
    HAIR_MAKEUP,
    DRESS,
    MENS_SUIT,
    BOUQUET,
    VIDEO,
    STUDIO,
    ETC,
    ;

    companion object {
        fun from(value: String): StudioPartnerShopCategory {
            return StudioPartnerShopCategory.entries.find { it.name == value }
                ?: throw IllegalArgumentException("유효하지 않은 StudioPartnerShopCategory 값: $value")
        }
    }
}
