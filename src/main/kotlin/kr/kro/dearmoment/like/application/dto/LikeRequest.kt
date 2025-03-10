package kr.kro.dearmoment.like.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.like.application.command.SaveLikeCommand
import org.jetbrains.annotations.NotNull

data class LikeRequest(
    @Schema(description = "사용자 ID", example = "1", required = true)
    @field:NotNull(value = "유저 ID 널이 될 수 없습니다.")
    val userId: Long,
    @Schema(description = "좋아요 대상 ID", example = "1", required = true)
    @field:NotNull(value = "좋아요 대상 ID는 널이 될 수 없습니다.")
    val targetId: Long,
) {
    fun toCommand() =
        SaveLikeCommand(
            userId = userId,
            targetId = targetId,
        )
}
