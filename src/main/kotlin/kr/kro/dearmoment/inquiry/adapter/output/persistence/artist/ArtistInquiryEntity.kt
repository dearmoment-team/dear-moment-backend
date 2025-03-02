package kr.kro.dearmoment.inquiry.adapter.output.persistence.artist

import Auditable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.kro.dearmoment.inquiry.domain.ArtistInquiry

@Entity
@Table(name = "artist_inquires")
class ArtistInquiryEntity(
    @Id
    @Column(name = "inquiry_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column
    val userId: Long,
    @Column(nullable = false)
    val title: String,
    @Column(nullable = false)
    val content: String,
) : Auditable() {
    fun toDomain() =
        ArtistInquiry(
            id = id,
            userId = userId,
            title = title,
            content = content,
            createdDate = createdDate ?: throw IllegalStateException("createdDate is null"),
        )

    companion object {
        fun from(inquiry: ArtistInquiry) =
            ArtistInquiryEntity(
                userId = inquiry.userId,
                title = inquiry.title,
                content = inquiry.content,
            )
    }
}
