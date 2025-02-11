package kr.kro.dearmoment.common.dto

import org.springframework.http.HttpStatus

data class SuccessResponse<T : Any>(
    val success: Boolean = true,
    val code: Int = HttpStatus.OK.value(),
    val data: T? = null,
)

data class ErrorResponse(val message: String)
