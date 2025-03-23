package kr.kro.dearmoment.like.application.command

data class UnlikeProductOptionCommand(
    val likeId: Long,
    val productOptionId: Long,
)
