package kr.kro.dearmoment.inquiry.adapter.output.persistence.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.inquiry.domain.CreateProductOptionInquiry
import kr.kro.dearmoment.inquiry.domain.ProductOptionInquiry
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionEntity
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "product_option_inquires")
class ProductOptionInquiryEntity(
    @Id
    @Column(name = "inquiry_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column
    val userId: UUID,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    val option: ProductOptionEntity,
) : Auditable() {
    fun toDomain(): ProductOptionInquiry {
        val product = option.product
        val productId = option.product.productId ?: throw IllegalStateException("product is is null")
        val studio = product.studio

        return ProductOptionInquiry(
            id = id,
            productId = productId,
            userId = userId,
            studioName = studio.name,
            optionName = option.name,
            thumbnailUrl = product.mainImage.url,
            createdDate = createdDate ?: LocalDateTime.now(),
        )
    }

    companion object {
        fun from(
            inquiry: CreateProductOptionInquiry,
            option: ProductOptionEntity,
        ) = ProductOptionInquiryEntity(
            userId = inquiry.userId,
            option = option,
        )
    }
}
