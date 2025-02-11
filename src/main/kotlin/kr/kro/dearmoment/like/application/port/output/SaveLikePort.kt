package kr.kro.dearmoment.like.application.port.output

import kr.kro.dearmoment.like.domain.Like

interface SaveLikePort {
    fun save(like: Like): Long
}
