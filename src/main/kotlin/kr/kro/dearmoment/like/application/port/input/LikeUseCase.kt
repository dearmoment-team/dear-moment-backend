package kr.kro.dearmoment.like.application.port.input

import kr.kro.dearmoment.like.adapter.input.web.dto.LikeResponse
import kr.kro.dearmoment.like.application.command.LikeCommand

interface LikeUseCase {
    fun like(command: LikeCommand): LikeResponse

    fun unlike(likeId: Long)
}
