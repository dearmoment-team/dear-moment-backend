package kr.kro.dearmoment.inquiry.adapter.output.persistence.author

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.kro.dearmoment.common.persistence.Auditable
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
) : Auditable() {
    @Column(nullable = false)
    var answer: String = ""
        protected set

    fun modifyAnswer(answer: String) {
        this.answer = answer
    }

    fun toDomain() =
        AuthorInquiry(
            id = id,
            userId = userId,
            title = title,
            content = content,
            answer = answer,
            createdDate = createdDate ?: throw IllegalStateException("createdDate is null"),
        )

    companion object {
        fun from(inquiry: AuthorInquiry) =
            AuthorInquiryEntity(
                userId = inquiry.userId,
                title = inquiry.title,
                content = inquiry.content,
            )
    }
}
