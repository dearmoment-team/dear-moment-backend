package kr.kro.dearmoment.inquiry.application.command

import kr.kro.dearmoment.inquiry.domain.AuthorInquiry

data class CreateAuthorInquiryCommand(
    val userId: Long,
    val title: String,
    val content: String,
) {
    companion object {
        fun toDomain(command: CreateAuthorInquiryCommand) =
            AuthorInquiry(
                userId = command.userId,
                title = command.title,
                content = command.content,
            )
    }
}
