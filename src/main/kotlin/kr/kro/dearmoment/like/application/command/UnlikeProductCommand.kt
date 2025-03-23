package kr.kro.dearmoment.like.application.command

data class UnlikeProductCommand(
    val likeId: Long,
    val productId: Long,
)
