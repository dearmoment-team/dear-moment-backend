package kr.kro.dearmoment.inquiry.adapter.output.persistence.studio

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.inquiry.domain.StudioInquiry
import java.util.UUID

@Entity
@Table(name = "studio_inquires")
class StudioInquiryEntity(
    @Id
    @Column(name = "inquiry_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column
    val userId: UUID,
    @Column(nullable = false)
    val title: String,
    @Column(nullable = false)
    val content: String,
) : Auditable() {
    fun toDomain() =
        StudioInquiry(
            id = id,
            userId = userId,
            title = title,
            content = content,
            createdDate = createdDate ?: throw IllegalStateException("createdDate is null"),
        )

    companion object {
        fun from(inquiry: StudioInquiry) =
            StudioInquiryEntity(
                userId = inquiry.userId,
                title = inquiry.title,
                content = inquiry.content,
            )
    }
}
