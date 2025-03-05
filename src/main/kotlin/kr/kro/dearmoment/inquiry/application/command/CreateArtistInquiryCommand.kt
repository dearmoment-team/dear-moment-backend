package kr.kro.dearmoment.inquiry.application.command

import kr.kro.dearmoment.inquiry.domain.ArtistInquiry

data class CreateArtistInquiryCommand(
    val userId: Long,
    val title: String,
    val content: String,
    val email: String,
) {
    fun toDomain() =
        ArtistInquiry(
            userId = userId,
            title = title,
            content = content,
        )
}
