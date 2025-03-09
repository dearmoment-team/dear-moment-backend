package kr.kro.dearmoment.inquiry.application.command

import kr.kro.dearmoment.inquiry.domain.StudioInquiry

data class CreateStudioInquiryCommand(
    val userId: Long,
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
