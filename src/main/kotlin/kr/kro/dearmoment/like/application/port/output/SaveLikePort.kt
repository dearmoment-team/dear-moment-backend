package kr.kro.dearmoment.like.application.port.output

import kr.kro.dearmoment.like.domain.CreateProductLike
import kr.kro.dearmoment.like.domain.CreateProductOptionLike

interface SaveLikePort {
    fun saveProductLike(like: CreateProductLike): Long

    fun saveProductOptionLike(like: CreateProductOptionLike): Long
}
