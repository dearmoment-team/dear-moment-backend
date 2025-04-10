package kr.kro.dearmoment.like.application.port.input

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.like.application.dto.GetProductLikeResponse
import kr.kro.dearmoment.like.application.dto.GetProductOptionLikeResponse
import kr.kro.dearmoment.like.application.query.ExistLikeQuery
import kr.kro.dearmoment.like.application.query.FilterUserLikesQuery
import kr.kro.dearmoment.like.application.query.GetUserProductLikeQuery
import kr.kro.dearmoment.like.application.query.GetUserProductOptionLikeQuery
import java.util.UUID

interface LikeQueryUseCase {
    fun getUserProductLikes(query: GetUserProductLikeQuery): PagedResponse<GetProductLikeResponse>

    fun getUserProductOptionLikes(query: GetUserProductOptionLikeQuery): PagedResponse<GetProductOptionLikeResponse>

    fun filterUserProductsLikes(
        userId: UUID,
        query: FilterUserLikesQuery,
    ): List<GetProductLikeResponse>

    fun filterUserProductsOptionsLikes(
        userId: UUID,
        query: FilterUserLikesQuery,
    ): List<GetProductOptionLikeResponse>

    fun isProductOptionLike(query: ExistLikeQuery): Boolean

    fun isProductLike(query: ExistLikeQuery): Boolean
}
