package kr.kro.dearmoment.like.application.port.output

import kr.kro.dearmoment.like.domain.Like

interface GetLikePort {
    fun loadLikes(userId: Long): List<Like>

    fun existLike(
        userId: Long,
        targetId: Long,
        type: String,
    ): Boolean
}
