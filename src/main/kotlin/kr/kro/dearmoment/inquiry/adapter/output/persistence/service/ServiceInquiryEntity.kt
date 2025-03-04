package kr.kro.dearmoment.inquiry.adapter.output.persistence.service

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.inquiry.domain.ServiceInquiry

@Entity
@Table(name = "service_inquires")
class ServiceInquiryEntity(
    @Id
    @Column(name = "inquiry_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(nullable = false)
    val type: String,
    @Column(nullable = false)
    val content: String,
) : Auditable() {
    companion object {
        fun from(inquiry: ServiceInquiry) =
            ServiceInquiryEntity(
                type = inquiry.type.name,
                content = inquiry.content,
            )
    }
}
