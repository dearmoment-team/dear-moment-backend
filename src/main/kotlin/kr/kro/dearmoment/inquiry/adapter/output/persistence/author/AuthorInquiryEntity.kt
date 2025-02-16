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
    @Column(nullable = false)
    val title: String,
    @Column(nullable = false)
    val content: String,
    @Column(nullable = false)
    val answered: Boolean = false,
) : Auditable() {
    companion object {
        fun from(inquiry: AuthorInquiry) =
            AuthorInquiryEntity(
                title = inquiry.title,
                content = inquiry.content,
            )
    }
}
