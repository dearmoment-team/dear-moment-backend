package kr.kro.dearmoment.like.application.port.input

import kr.kro.dearmoment.like.application.dto.GetProductOptionLikeResponse
import kr.kro.dearmoment.like.application.dto.GetStudioLikeResponse
import kr.kro.dearmoment.like.application.query.ExistLikeQuery

interface LikeQueryUseCase {
    fun getUserStudioLikes(userId: Long): List<GetStudioLikeResponse>

    fun getUserProductOptionLikes(userId: Long): List<GetProductOptionLikeResponse>

    fun isProductOptionLike(query: ExistLikeQuery): Boolean

    fun isStudioLike(query: ExistLikeQuery): Boolean
}
