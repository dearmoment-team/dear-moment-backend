package kr.kro.dearmoment.like.application.query

import java.util.UUID

data class ExistLikeQuery(
    val userId: UUID,
    val targetId: Long,
)
