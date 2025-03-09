package kr.kro.dearmoment.like.application.dto

import kr.kro.dearmoment.like.application.command.SaveLikeCommand
import org.jetbrains.annotations.NotNull

data class LikeRequest(
    @field:NotNull(value = "유저 ID 널이 될 수 없습니다.")
    val userId: Long,
    @field:NotNull(value = "좋아요 대상 ID는 널이 될 수 없습니다.")
    val targetId: Long,
) {
    fun toCommand() =
        SaveLikeCommand(
            userId = userId,
            targetId = targetId,
        )
}
