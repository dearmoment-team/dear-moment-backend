package kr.kro.dearmoment.like.application.port.output

interface DeleteLikePort {
    fun deleteProductLike(likeId: Long)

    fun deleteProductOptionLike(likeId: Long)
}
