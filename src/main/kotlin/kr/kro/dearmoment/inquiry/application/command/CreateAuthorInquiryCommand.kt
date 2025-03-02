package kr.kro.dearmoment.inquiry.application.command

import kr.kro.dearmoment.inquiry.domain.AuthorInquiry

data class CreateAuthorInquiryCommand(
    val userId: Long,
    val title: String,
    val content: String,
    val email: String,
) {
    fun toDomain() =
        AuthorInquiry(
            userId = userId,
            title = title,
            content = content,
        )
}
