package kr.kro.dearmoment.like.application.command

import java.util.UUID

data class UnlikeProductOptionCommand(
    val userId: UUID,
    val likeId: Long,
    val productOptionId: Long,
)
