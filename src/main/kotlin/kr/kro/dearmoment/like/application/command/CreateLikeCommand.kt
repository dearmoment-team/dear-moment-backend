package kr.kro.dearmoment.like.application.command

data class CreateLikeCommand(
    val userId: Long,
    val targetId: Long,
    val type: String,
)
