package kr.kro.dearmoment.product.domain.model.option

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode

/**
 * 제휴업체 구분(카테고리)
 */
enum class PartnerShopCategory {
    HAIR_MAKEUP,
    DRESS,
    MENS_SUIT,
    BOUQUET,
    VIDEO,
    STUDIO,
    ETC,
    ;

    companion object {
        fun from(value: String): PartnerShopCategory =
            try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                throw CustomException(ErrorCode.INVALID_PARTNER_SHOP_CATEGORY)
            }
    }
}
