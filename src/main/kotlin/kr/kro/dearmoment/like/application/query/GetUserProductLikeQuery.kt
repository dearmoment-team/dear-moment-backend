package kr.kro.dearmoment.like.application.query

import org.springframework.data.domain.Pageable
import java.util.UUID

data class GetUserProductLikeQuery(
    val userId: UUID,
    val pageable: Pageable,
)
