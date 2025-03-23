package kr.kro.dearmoment.like.application.port.input

import kr.kro.dearmoment.like.application.command.SaveLikeCommand
import kr.kro.dearmoment.like.application.command.UnlikeProductCommand
import kr.kro.dearmoment.like.application.dto.LikeResponse

interface LikeUseCase {
    fun productLike(command: SaveLikeCommand): LikeResponse

    fun productOptionsLike(command: SaveLikeCommand): LikeResponse

    fun productUnlike(command: UnlikeProductCommand)

    fun productOptionUnlike(likeId: Long)
}
