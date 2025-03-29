package kr.kro.dearmoment.like.application.port.output

import java.util.UUID

interface DeleteLikePort {
    fun deleteProductLike(
        userId: UUID,
        likeId: Long,
    )

    fun deleteProductOptionLike(
        userId: UUID,
        likeId: Long,
    )
}
