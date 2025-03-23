package kr.kro.dearmoment.like.application.dto

import kr.kro.dearmoment.like.application.command.UnlikeProductOptionCommand

data class UnlikeProductOptionRequest(
    val likeId: Long,
    val productOptionId: Long,
) {
    fun toCommand() =
        UnlikeProductOptionCommand(
            likeId = likeId,
            productOptionId = productOptionId,
        )
}
