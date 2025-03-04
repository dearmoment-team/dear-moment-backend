package kr.kro.dearmoment.like.application.dto

import jakarta.validation.constraints.NotBlank
import org.jetbrains.annotations.NotNull

data class LikeRequest(
    @field:NotNull(value = "유저 ID 널이 될 수 없습니다.")
    val userId: Long,
    @field:NotNull(value = "좋아요 대상 ID는 널이 될 수 없습니다.")
    val targetId: Long,
    /***
     * LikeType 참고
     */
    @field:NotBlank(message = "좋아요 타입은 빈 문자열이 될 수 없습니다.")
    val type: String,
)
