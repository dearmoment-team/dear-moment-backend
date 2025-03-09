package kr.kro.dearmoment.like.application.port.input

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.like.application.dto.GetProductOptionLikeResponse
import kr.kro.dearmoment.like.application.dto.GetStudioLikeResponse
import kr.kro.dearmoment.like.application.query.ExistLikeQuery
import kr.kro.dearmoment.like.application.query.GetUserProductOptionLikeQuery
import kr.kro.dearmoment.like.application.query.GetUserStudioLikeQuery

interface LikeQueryUseCase {
    fun getUserStudioLikes(query: GetUserStudioLikeQuery): PagedResponse<GetStudioLikeResponse>

    fun getUserProductOptionLikes(query: GetUserProductOptionLikeQuery): PagedResponse<GetProductOptionLikeResponse>

    fun isProductOptionLike(query: ExistLikeQuery): Boolean

    fun isStudioLike(query: ExistLikeQuery): Boolean
}
