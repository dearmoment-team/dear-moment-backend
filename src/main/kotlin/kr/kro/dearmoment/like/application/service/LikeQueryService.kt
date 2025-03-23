package kr.kro.dearmoment.like.application.service

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.like.application.dto.GetProductLikeResponse
import kr.kro.dearmoment.like.application.dto.GetProductOptionLikeResponse
import kr.kro.dearmoment.like.application.port.input.LikeQueryUseCase
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.like.application.query.ExistLikeQuery
import kr.kro.dearmoment.like.application.query.GetUserProductLikeQuery
import kr.kro.dearmoment.like.application.query.GetUserProductOptionLikeQuery
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class LikeQueryService(
    private val getLikePort: GetLikePort,
) : LikeQueryUseCase {
    override fun getUserProductLikes(query: GetUserProductLikeQuery): PagedResponse<GetProductLikeResponse> {
        val userLikes = getLikePort.findUserProductLikes(query.userId, query.pageable)
        return PagedResponse(
            content = userLikes.content.map { GetProductLikeResponse.from(it) },
            page = query.pageable.pageNumber,
            size = query.pageable.pageSize,
            totalElements = userLikes.totalElements,
            totalPages = userLikes.totalPages,
        )
    }

    override fun getUserProductOptionLikes(query: GetUserProductOptionLikeQuery): PagedResponse<GetProductOptionLikeResponse> {
        val userLikes = getLikePort.findUserProductOptionLikes(query.userId, query.pageable)
        return PagedResponse(
            content = userLikes.content.map { GetProductOptionLikeResponse.from(it) },
            page = query.pageable.pageNumber,
            size = query.pageable.pageSize,
            totalElements = userLikes.totalElements,
            totalPages = userLikes.totalPages,
        )
    }

    override fun isProductLike(query: ExistLikeQuery): Boolean = getLikePort.existProductLike(query.userId, query.targetId)

    override fun isProductOptionLike(query: ExistLikeQuery): Boolean = getLikePort.existProductOptionLike(query.userId, query.targetId)
}
