package kr.kro.dearmoment.like.application.command

data class SaveLikeCommand(
    val userId: Long,
    val targetId: Long,
    val type: String,
)
