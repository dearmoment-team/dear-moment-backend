package kr.kro.dearmoment.inquiry.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.inquiry.domain.StudioInquiry
import java.time.LocalDateTime

data class GetStudioInquiryResponse(
    @Schema(description = "문의 ID", example = "1")
    val inquiryId: Long,
    @Schema(description = "제목", example = "스튜디오 인스타 접근 불가능 문의")
    val title: String,
    @Schema(description = "내용", example = "확인 부탁드립니다.")
    val content: String,
    @Schema(description = "생성 날짜", example = "2025-03-09T15:45:30")
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
