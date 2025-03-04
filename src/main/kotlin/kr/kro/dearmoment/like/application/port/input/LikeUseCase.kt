package kr.kro.dearmoment.like.application.port.input

import kr.kro.dearmoment.like.application.command.SaveLikeCommand
import kr.kro.dearmoment.like.application.dto.GetLikeResponse
import kr.kro.dearmoment.like.application.dto.LikeResponse
import kr.kro.dearmoment.like.application.query.ExistLikeQuery
import kr.kro.dearmoment.like.application.query.GetLikesQuery

interface LikeUseCase {
    fun getLikes(query: GetLikesQuery): List<GetLikeResponse>

    fun isLike(query: ExistLikeQuery): Boolean

    fun like(command: SaveLikeCommand): LikeResponse

    fun unlike(likeId: Long)
}
