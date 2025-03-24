package kr.kro.dearmoment.product.domain.model

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode

/**
 * 카메라 종류
 */
enum class CameraType {
    DIGITAL,
    FILM,
    ;

    companion object {
        fun from(value: String): CameraType =
            try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                throw CustomException(ErrorCode.INVALID_CAMERA_TYPE)
            }
    }
}
