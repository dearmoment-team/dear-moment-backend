package kr.kro.dearmoment.like.application.dto

import kr.kro.dearmoment.like.application.command.UnlikeProductOptionCommand
import java.util.UUID

data class UnlikeProductOptionRequest(
    val likeId: Long,
    val productOptionId: Long,
) {
    fun toCommand(userId: UUID) =
        UnlikeProductOptionCommand(
            userId = userId,
            likeId = likeId,
            productOptionId = productOptionId,
        )
}
