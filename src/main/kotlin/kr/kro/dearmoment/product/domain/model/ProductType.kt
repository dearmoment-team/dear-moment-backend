package kr.kro.dearmoment.product.domain.model

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode

/**
 * 상품 유형 (예: 웨딩스냅)
 */
enum class ProductType {
    WEDDING_SNAP,
    ;

    companion object {
        fun from(value: String): ProductType =
            try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                throw CustomException(ErrorCode.INVALID_PRODUCT_TYPE)
            }
    }
}
