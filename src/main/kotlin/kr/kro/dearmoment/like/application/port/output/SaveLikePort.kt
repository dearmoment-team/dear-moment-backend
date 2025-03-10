package kr.kro.dearmoment.like.application.port.output

import kr.kro.dearmoment.like.domain.CreateProductOptionLike
import kr.kro.dearmoment.like.domain.CreateStudioLike

interface SaveLikePort {
    fun saveStudioLike(like: CreateStudioLike): Long

    fun saveProductOptionLike(like: CreateProductOptionLike): Long
}
