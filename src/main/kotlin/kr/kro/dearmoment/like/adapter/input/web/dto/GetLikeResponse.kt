package kr.kro.dearmoment.like.adapter.input.web.dto

import kr.kro.dearmoment.like.domain.Like

data class GetLikeResponse(
    val likeId: Long,
    val targetId: Long,
) {
    companion object {
        fun from(like: Like) =
            GetLikeResponse(
                likeId = like.id,
                targetId = like.targetId,
            )
    }
}
