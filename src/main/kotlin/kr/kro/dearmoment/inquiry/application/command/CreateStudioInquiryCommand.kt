package kr.kro.dearmoment.inquiry.application.command

import kr.kro.dearmoment.inquiry.domain.StudioInquiry
import java.util.UUID

data class CreateStudioInquiryCommand(
    val userId: UUID,
    val title: String,
    val content: String,
    val email: String,
) {
    fun toDomain() =
        StudioInquiry(
            userId = userId,
            title = title,
            content = content,
        )
}
