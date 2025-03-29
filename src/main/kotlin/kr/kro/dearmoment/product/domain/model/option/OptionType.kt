package kr.kro.dearmoment.product.domain.model.option

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode

/**
 * 옵션의 타입(단품 or 패키지)
 */
enum class OptionType {
    SINGLE,
    PACKAGE,
    ;

    companion object {
        fun from(value: String): OptionType =
            try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                throw CustomException(ErrorCode.INVALID_OPTION_TYPE)
            }
    }
}
