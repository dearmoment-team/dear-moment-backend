package kr.kro.dearmoment.like.application.port.output

import kr.kro.dearmoment.like.domain.ProductOptionLike
import kr.kro.dearmoment.like.domain.StudioLike
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetLikePort {
    fun findUserStudioLikes(
        userId: Long,
        pageable: Pageable,
    ): Page<StudioLike>

    fun findUserProductOptionLikes(
        userId: Long,
        pageable: Pageable,
    ): Page<ProductOptionLike>

    fun existStudioLike(
        userId: Long,
        studioId: Long,
    ): Boolean

    fun existProductOptionLike(
        userId: Long,
        productOptionId: Long,
    ): Boolean
}
