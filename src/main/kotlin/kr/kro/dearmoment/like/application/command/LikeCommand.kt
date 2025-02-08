package kr.kro.dearmoment.like.application.command

data class LikeCommand(
    val userId: Long,
    val targetId: Long,
    val type: String,
)
