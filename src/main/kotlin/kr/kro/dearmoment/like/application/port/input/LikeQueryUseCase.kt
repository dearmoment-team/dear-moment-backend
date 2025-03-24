package kr.kro.dearmoment.like.application.port.input

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.like.application.dto.GetProductLikeResponse
import kr.kro.dearmoment.like.application.dto.GetProductOptionLikeResponse
import kr.kro.dearmoment.like.application.query.ExistLikeQuery
import kr.kro.dearmoment.like.application.query.GetUserProductLikeQuery
import kr.kro.dearmoment.like.application.query.GetUserProductOptionLikeQuery

interface LikeQueryUseCase {
    fun getUserProductLikes(query: GetUserProductLikeQuery): PagedResponse<GetProductLikeResponse>

    fun getUserProductOptionLikes(query: GetUserProductOptionLikeQuery): PagedResponse<GetProductOptionLikeResponse>

    fun isProductOptionLike(query: ExistLikeQuery): Boolean

    fun isProductLike(query: ExistLikeQuery): Boolean
}
