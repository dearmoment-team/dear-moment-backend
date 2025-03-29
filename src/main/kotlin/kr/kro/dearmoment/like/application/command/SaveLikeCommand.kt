package kr.kro.dearmoment.like.application.command

import kr.kro.dearmoment.like.domain.CreateProductLike
import kr.kro.dearmoment.like.domain.CreateProductOptionLike
import java.util.UUID

data class SaveLikeCommand(
    val userId: UUID,
    val targetId: Long,
) {
    fun toStudioLikeDomain() =
        CreateProductLike(
            userId = userId,
            productId = targetId,
        )

    fun toProductOptionLikeDomain() =
        CreateProductOptionLike(
            userId = userId,
            productOptionId = targetId,
        )
}
