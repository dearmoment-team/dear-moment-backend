package kr.kro.dearmoment.inquiry.application.dto

import kr.kro.dearmoment.inquiry.domain.ArtistInquiry
import java.time.LocalDateTime

data class GetArtistInquiryResponse(
    val inquiryId: Long,
    val title: String,
    val content: String,
    val createdDate: LocalDateTime,
) {
    companion object {
        fun from(inquiry: ArtistInquiry) =
            GetArtistInquiryResponse(
                inquiryId = inquiry.id,
                title = inquiry.title,
                content = inquiry.content,
                createdDate = inquiry.createdDate,
            )
    }
}
