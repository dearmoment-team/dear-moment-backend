package kr.kro.dearmoment.like.application.query

import org.springframework.data.domain.Pageable

data class GetUserStudioLikeQuery(
    val userId: Long,
    val pageable: Pageable,
)
