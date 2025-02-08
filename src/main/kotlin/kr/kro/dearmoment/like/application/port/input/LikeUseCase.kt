package kr.kro.dearmoment.like.application.port.input

import kr.kro.dearmoment.like.application.command.LikeCommand

interface LikeUseCase {
    fun like(command: LikeCommand): Long

    fun unlike(likeId: Long): Long
}
