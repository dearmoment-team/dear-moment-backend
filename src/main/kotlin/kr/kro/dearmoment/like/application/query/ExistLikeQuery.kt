package kr.kro.dearmoment.like.application.query

data class ExistLikeQuery(
    val userId: Long,
    val targetId: Long,
)
