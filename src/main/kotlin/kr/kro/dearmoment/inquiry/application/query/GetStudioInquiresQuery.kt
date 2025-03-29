package kr.kro.dearmoment.inquiry.application.query

import org.springframework.data.domain.Pageable
import java.util.UUID

data class GetStudioInquiresQuery(
    val userId: UUID,
    val pageable: Pageable,
)
