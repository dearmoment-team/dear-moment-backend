package kr.kro.dearmoment.like.application.dto

import kr.kro.dearmoment.like.application.command.UnlikeProductCommand
import java.util.UUID

data class UnlikeProductRequest(
    val likeId: Long,
    val productId: Long,
) {
    fun toCommand(userId: UUID) =
        UnlikeProductCommand(
            userId = userId,
            likeId = likeId,
            productId = productId,
        )
}
