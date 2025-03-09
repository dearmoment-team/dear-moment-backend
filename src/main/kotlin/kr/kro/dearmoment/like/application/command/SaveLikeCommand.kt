package kr.kro.dearmoment.like.application.command

import kr.kro.dearmoment.like.domain.CreateProductOptionLike
import kr.kro.dearmoment.like.domain.CreateStudioLike

data class SaveLikeCommand(
    val userId: Long,
    val targetId: Long,
) {
    fun toStudioLikeDomain() =
        CreateStudioLike(
            userId = userId,
            studioId = targetId,
        )

    fun toProductOptionLikeDomain() =
        CreateProductOptionLike(
            userId = userId,
            productOptionId = targetId,
        )
}
