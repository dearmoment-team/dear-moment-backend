package kr.kro.dearmoment.like.application.service

import kr.kro.dearmoment.like.application.command.SaveLikeCommand
import kr.kro.dearmoment.like.application.dto.LikeResponse
import kr.kro.dearmoment.like.application.port.input.LikeUseCase
import kr.kro.dearmoment.like.application.port.output.DeleteLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LikeCommandService(
    private val saveLikePort: SaveLikePort,
    private val deleteLikePort: DeleteLikePort,
) : LikeUseCase {
    override fun studioLike(command: SaveLikeCommand): LikeResponse {
        val like = command.toStudioLikeDomain()
        return LikeResponse(saveLikePort.saveStudioLike(like))
    }

    override fun productOptionsLike(command: SaveLikeCommand): LikeResponse {
        val like = command.toProductOptionLikeDomain()
        return LikeResponse(saveLikePort.saveProductOptionLike(like))
    }

    override fun studioUnlike(likeId: Long) {
        deleteLikePort.deleteStudioLike(likeId)
    }

    override fun productOptionUnlike(likeId: Long) {
        deleteLikePort.deleteProductOptionLike(likeId)
    }
}
