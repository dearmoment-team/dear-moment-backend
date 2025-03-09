package kr.kro.dearmoment.like.application.port.output

interface DeleteLikePort {
    fun deleteStudioLike(likeId: Long)

    fun deleteProductOptionLike(likeId: Long)
}
