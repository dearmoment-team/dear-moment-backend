package kr.kro.dearmoment.inquiry.application.query

import org.springframework.data.domain.Pageable

data class GetAuthorInquiresQuery(
    val userId: Long,
    val pageable: Pageable,
)
