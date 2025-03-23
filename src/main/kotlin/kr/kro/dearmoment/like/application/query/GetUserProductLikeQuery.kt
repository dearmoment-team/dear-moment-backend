package kr.kro.dearmoment.like.application.query

import org.springframework.data.domain.Pageable

data class GetUserProductLikeQuery(
    val userId: Long,
    val pageable: Pageable,
)
