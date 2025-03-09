package kr.kro.dearmoment.like.application.dto

import io.swagger.v3.oas.annotations.media.Schema

data class LikeResponse(
    @Schema(description = "좋아요 ID", example = "1")
    val likeId: Long,
)
