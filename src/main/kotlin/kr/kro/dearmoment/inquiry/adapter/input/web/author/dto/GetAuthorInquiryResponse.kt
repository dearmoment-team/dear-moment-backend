package kr.kro.dearmoment.inquiry.adapter.input.web.author.dto

import kr.kro.dearmoment.inquiry.domain.AuthorInquiry
import java.time.LocalDateTime

data class GetAuthorInquiryResponse(
    val inquiryId: Long,
    val title: String,
    val content: String,
    val createdDate: LocalDateTime,
) {
    companion object {
        fun from(inquiry: AuthorInquiry) =
            GetAuthorInquiryResponse(
                inquiryId = inquiry.id,
                title = inquiry.title,
                content = inquiry.content,
                createdDate = inquiry.createdDate,
            )
    }
}
