package kr.kro.onboarding.common.dto

import org.springframework.http.HttpStatus

data class BaseResponse<T : Any>(
    val success: Boolean,
    val code: Int = HttpStatus.OK.value(),
    val data: T? = null,
)
