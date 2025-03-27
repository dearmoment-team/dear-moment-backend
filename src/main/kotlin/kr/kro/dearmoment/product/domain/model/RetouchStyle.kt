package kr.kro.dearmoment.product.domain.model

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode

/**
 * 보정 스타일
 */
enum class RetouchStyle {
    MODERN,
    CHIC,
    CALM,
    VINTAGE,
    FAIRYTALE,
    WARM,
    DREAMY,
    BRIGHT,
    NATURAL,
    ;

    companion object {
        fun from(value: String): RetouchStyle =
            try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                throw CustomException(ErrorCode.INVALID_RETOUCH_STYLE)
            }
    }
}
