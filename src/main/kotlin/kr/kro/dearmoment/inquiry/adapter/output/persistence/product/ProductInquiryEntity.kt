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
    @Column(nullable = false)
    val productId: Long,
) : Auditable() {
    companion object {
        fun from(inquiry: ProductInquiry) =
            ProductInquiryEntity(
                productId = inquiry.productId,
            )
    }
}
