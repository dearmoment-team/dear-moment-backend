package kr.kro.dearmoment.product.domain.model

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode

/**
 * 촬영 가능 시기
 */
enum class ShootingSeason {
    YEAR_2025_FIRST_HALF,
    YEAR_2025_SECOND_HALF,
    YEAR_2026_FIRST_HALF,
    YEAR_2026_SECOND_HALF,
    ;

    companion object {
        fun from(value: String): ShootingSeason =
            try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                throw CustomException(ErrorCode.INVALID_SEASON)
            }
    }
}
