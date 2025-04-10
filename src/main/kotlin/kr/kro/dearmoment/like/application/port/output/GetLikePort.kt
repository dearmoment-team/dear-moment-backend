package kr.kro.dearmoment.like.application.port.output

import kr.kro.dearmoment.like.domain.ProductLike
import kr.kro.dearmoment.like.domain.ProductOptionLike
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface GetLikePort {
    fun findUserProductLikes(
        userId: UUID,
        pageable: Pageable,
    ): Page<ProductLike>

    fun findUserProductOptionLikes(
        userId: UUID,
        pageable: Pageable,
    ): Page<ProductOptionLike>

    fun findUserProductLikes(userId: UUID): List<ProductLike>

    fun findUserProductOptionLikes(userId: UUID): List<ProductOptionLike>

    fun existProductLike(
        userId: UUID,
        productId: Long,
    ): Boolean

    fun existProductOptionLike(
        userId: UUID,
        productOptionId: Long,
    ): Boolean

    fun findOptionLikesByUserIdAndOptionIds(
        userId: UUID,
        productOptionId: List<Long>,
    ): List<ProductOptionLike>

    fun findProductLikesByUserIdAndProductIds(
        userId: UUID,
        productIds: List<Long>,
    ): List<ProductLike>

    fun findProductLikesByUserIdAndProductId(
        userId: UUID,
        productId: Long,
    ): ProductLike?
}
