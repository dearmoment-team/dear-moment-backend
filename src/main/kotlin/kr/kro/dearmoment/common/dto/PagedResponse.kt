package kr.kro.dearmoment.common.dto

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0L,
    val totalPages: Int = 0,
)
