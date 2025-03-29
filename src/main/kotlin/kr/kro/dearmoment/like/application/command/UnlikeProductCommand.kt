package kr.kro.dearmoment.like.application.command

import java.util.UUID

data class UnlikeProductCommand(
    val userId: UUID,
    val likeId: Long,
    val productId: Long,
)
