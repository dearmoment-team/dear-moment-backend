package kr.kro.dearmoment.product.domain.model

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode

/**
 * 촬영 장소 (예: JEJU)
 */
enum class ShootingPlace {
    JEJU,
    ;

    companion object {
        fun from(value: String): ShootingPlace =
            try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                throw CustomException(ErrorCode.INVALID_SHOOTING_PLACE)
            }
    }
}
