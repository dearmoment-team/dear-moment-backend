package kr.kro.dearmoment.like.application.port.input

import kr.kro.dearmoment.like.application.command.CreateLikeCommand

interface LikeUseCase {
    fun like(command: CreateLikeCommand): Long

    fun unlike(likeId: Long): Long
}
