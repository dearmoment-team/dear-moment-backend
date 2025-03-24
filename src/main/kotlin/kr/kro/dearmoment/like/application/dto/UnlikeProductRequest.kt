package kr.kro.dearmoment.like.application.dto

import kr.kro.dearmoment.like.application.command.UnlikeProductCommand

data class UnlikeProductRequest(
    val likeId: Long,
    val productId: Long,
) {
    fun toCommand() =
        UnlikeProductCommand(
            likeId = likeId,
            productId = productId,
        )
}
