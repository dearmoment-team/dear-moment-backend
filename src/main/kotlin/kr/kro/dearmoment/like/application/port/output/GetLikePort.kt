package kr.kro.dearmoment.like.application.port.output

import kr.kro.dearmoment.like.domain.ProductOptionLike
import kr.kro.dearmoment.like.domain.StudioLike

interface GetLikePort {
    fun findUserStudioLikes(userId: Long): List<StudioLike>

    fun findUserProductLikes(userId: Long): List<ProductOptionLike>

    fun existStudioLike(
        userId: Long,
        studioId: Long,
    ): Boolean

    fun existProductOptionLike(
        userId: Long,
        productOptionId: Long,
    ): Boolean
}
