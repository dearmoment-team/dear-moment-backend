package kr.kro.dearmoment.like.application.port.input

import kr.kro.dearmoment.like.adapter.input.web.dto.LikeResponse
import kr.kro.dearmoment.like.application.command.LikeCommand
import kr.kro.dearmoment.like.application.query.ExistLikeQuery
import kr.kro.dearmoment.like.application.query.GetLikesQuery

interface LikeUseCase {
    fun getLikes(query: GetLikesQuery): List<Long>

    fun isLike(query: ExistLikeQuery): Boolean

    fun like(command: LikeCommand): LikeResponse

    fun unlike(likeId: Long)
}
