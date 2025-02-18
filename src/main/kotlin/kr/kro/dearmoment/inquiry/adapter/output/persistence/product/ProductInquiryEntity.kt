package kr.kro.dearmoment.inquiry.adapter.output.persistence.product

import Auditable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.kro.dearmoment.inquiry.domain.ProductInquiry

@Entity
@Table(name = "product_inquires")
class ProductInquiryEntity(
    @Id
    @Column(name = "inquiry_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column
    val userId: Long,
    @Column(nullable = false)
    val productId: Long,
) : Auditable() {
    companion object {
        fun toDomain(entity: ProductInquiryEntity) =
            ProductInquiry(
                id = entity.id,
                userId = entity.userId,
                productId = entity.productId,
                thumbnailUrl = "",
                createdDate = entity.createdDate ?: throw IllegalStateException("createdDate is null"),
            )

        fun from(inquiry: ProductInquiry) =
            ProductInquiryEntity(
                userId = inquiry.userId,
                productId = inquiry.productId,
            )
    }
}
