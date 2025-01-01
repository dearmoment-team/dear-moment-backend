package kr.kro.onboarding.common.dto

import org.springframework.http.HttpStatus

data class BaseResponse<T : Any>(
    val success: Boolean,
    val code: Int = HttpStatus.OK.value(),
    val data: T? = null
) {
    companion object {
        fun <T : Any> success(data: T?): BaseResponse<T> {
            return BaseResponse(success = true, code = HttpStatus.OK.value(), data = data)
        }

        fun <T : Any> error(code: Int, data: T? = null): BaseResponse<T> {
            return BaseResponse(success = false, code = code, data = data)
        }
    }
}
