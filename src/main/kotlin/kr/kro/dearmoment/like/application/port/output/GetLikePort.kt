package kr.kro.dearmoment.like.application.port.output

import kr.kro.dearmoment.like.domain.ProductLike
import kr.kro.dearmoment.like.domain.ProductOptionLike
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetLikePort {
    fun findUserProductLikes(
        userId: Long,
        pageable: Pageable,
    ): Page<ProductLike>

    fun findUserProductOptionLikes(
        userId: Long,
        pageable: Pageable,
    ): Page<ProductOptionLike>

    fun existProductLike(
        userId: Long,
        productId: Long,
    ): Boolean

    fun existProductOptionLike(
        userId: Long,
        productOptionId: Long,
    ): Boolean
}
