package kr.kro.dearmoment.inquiry.application.dto

import kr.kro.dearmoment.inquiry.domain.StudioInquiry
import java.time.LocalDateTime

data class GetStudioInquiryResponse(
    val inquiryId: Long,
    val title: String,
    val content: String,
    val createdDate: LocalDateTime,
) {
    companion object {
        fun from(inquiry: StudioInquiry) =
            GetStudioInquiryResponse(
                inquiryId = inquiry.id,
                title = inquiry.title,
                content = inquiry.content,
                createdDate = inquiry.createdDate,
            )
    }
}
