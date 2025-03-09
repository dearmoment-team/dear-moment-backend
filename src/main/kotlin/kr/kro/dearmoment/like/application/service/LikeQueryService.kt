package kr.kro.dearmoment.like.application.service

import kr.kro.dearmoment.like.application.dto.GetProductOptionLikeResponse
import kr.kro.dearmoment.like.application.dto.GetStudioLikeResponse
import kr.kro.dearmoment.like.application.port.input.LikeQueryUseCase
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.like.application.query.ExistLikeQuery
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class LikeQueryService(
    private val getLikePort: GetLikePort,
) : LikeQueryUseCase {
    override fun getUserStudioLikes(userId: Long): List<GetStudioLikeResponse> =
        getLikePort.findUserStudioLikes(userId).map {
            GetStudioLikeResponse.from(it)
        }

    override fun getUserProductOptionLikes(userId: Long): List<GetProductOptionLikeResponse> =
        getLikePort.findUserProductLikes(userId).map {
            GetProductOptionLikeResponse.from(it)
        }

    override fun isStudioLike(query: ExistLikeQuery): Boolean = getLikePort.existStudioLike(query.userId, query.targetId)

    override fun isProductOptionLike(query: ExistLikeQuery): Boolean = getLikePort.existProductOptionLike(query.userId, query.targetId)
}
