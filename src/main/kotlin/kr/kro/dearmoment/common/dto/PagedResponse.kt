package kr.kro.dearmoment.common.dto

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int = 0,
)
