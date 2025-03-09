package kr.kro.dearmoment.like.application.port.input

import kr.kro.dearmoment.like.application.command.SaveLikeCommand
import kr.kro.dearmoment.like.application.dto.LikeResponse

interface LikeUseCase {
    fun studioLike(command: SaveLikeCommand): LikeResponse

    fun productOptionsLike(command: SaveLikeCommand): LikeResponse

    fun studioUnlike(likeId: Long)

    fun productOptionUnlike(likeId: Long)
}
