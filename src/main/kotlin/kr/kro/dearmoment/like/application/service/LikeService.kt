package kr.kro.dearmoment.like.application.service

import kr.kro.dearmoment.like.application.command.SaveLikeCommand
import kr.kro.dearmoment.like.application.dto.GetLikeResponse
import kr.kro.dearmoment.like.application.dto.LikeResponse
import kr.kro.dearmoment.like.application.port.input.LikeUseCase
import kr.kro.dearmoment.like.application.port.output.DeleteLikePort
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort
import kr.kro.dearmoment.like.application.query.ExistLikeQuery
import kr.kro.dearmoment.like.application.query.GetLikesQuery
import kr.kro.dearmoment.like.domain.Like
import kr.kro.dearmoment.like.domain.LikeType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeService(
    private val saveLikePort: SaveLikePort,
    private val deleteLikePort: DeleteLikePort,
    private val getLikePort: GetLikePort,
) : LikeUseCase {
    @Transactional
    override fun like(command: SaveLikeCommand): LikeResponse {
        val likeType = LikeType.from(command.type)
        val like = Like(userId = command.userId, targetId = command.targetId, type = likeType)

        return LikeResponse(saveLikePort.save(like))
    }

    @Transactional(readOnly = true)
    override fun getLikes(query: GetLikesQuery): List<GetLikeResponse> {
        val likeType = LikeType.from(query.likeType)

        return getLikePort.loadLikes(query.userId)
            .filter { it.type.value == likeType.value }
            .map { GetLikeResponse.from(it) }
    }

    @Transactional(readOnly = true)
    override fun isLike(query: ExistLikeQuery): Boolean = getLikePort.existLike(query.userId, query.targetId, query.type)

    @Transactional
    override fun unlike(likeId: Long) {
        deleteLikePort.delete(likeId)
    }
}
