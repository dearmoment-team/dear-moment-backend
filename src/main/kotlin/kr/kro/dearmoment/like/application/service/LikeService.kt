package kr.kro.dearmoment.like.application.service

import kr.kro.dearmoment.like.adapter.input.web.dto.LikeResponse
import kr.kro.dearmoment.like.application.command.LikeCommand
import kr.kro.dearmoment.like.application.port.input.LikeUseCase
import kr.kro.dearmoment.like.application.port.output.DeleteLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort
import kr.kro.dearmoment.like.domain.Like
import kr.kro.dearmoment.like.domain.LikeType
import org.springframework.stereotype.Service

@Service
class LikeService(
    private val saveLikePort: SaveLikePort,
    private val deleteLikePort: DeleteLikePort,
) : LikeUseCase {
    override fun like(command: LikeCommand): LikeResponse {
        val likeType = LikeType.from(command.type)
        val like = Like(userId = command.userId, targetId = command.targetId, type = likeType)

        return LikeResponse(likeId = saveLikePort.save(like))
    }

    override fun unlike(likeId: Long) {
        deleteLikePort.delete(likeId)
    }
}
