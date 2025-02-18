package kr.kro.dearmoment.inquiry.adapter.output.persistence.author

import Auditable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.kro.dearmoment.inquiry.domain.AuthorInquiry

@Entity
@Table(name = "author_inquires")
class AuthorInquiryEntity(
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
    @Column(nullable = false)
    val answer: String = "",
) : Auditable() {
    companion object {
        fun toDomain(entity: AuthorInquiryEntity) =
            AuthorInquiry(
                id = entity.id,
                userId = entity.userId,
                title = entity.title,
                content = entity.content,
                answer = entity.answer,
                createdDate = entity.createdDate ?: throw IllegalStateException("createdDate is null"),
            )

        fun from(inquiry: AuthorInquiry) =
            AuthorInquiryEntity(
                userId = inquiry.userId,
                title = inquiry.title,
                content = inquiry.content,
            )
    }
}
