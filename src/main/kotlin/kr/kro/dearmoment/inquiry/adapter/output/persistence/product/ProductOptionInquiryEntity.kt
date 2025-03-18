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
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.inquiry.domain.CreateProductOptionInquiry
import kr.kro.dearmoment.inquiry.domain.ProductOptionInquiry
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionEntity

@Entity
@Table(name = "product_option_inquires")
class ProductOptionInquiryEntity(
    @Id
    @Column(name = "inquiry_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column
    val userId: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    val option: ProductOptionEntity,
) : Auditable() {
    fun toCreateDomain(): CreateProductOptionInquiry =
        CreateProductOptionInquiry(
            id = id,
            userId = userId,
            productOptionId = option.optionId
        )

    fun toDomain(): ProductOptionInquiry {
        val product = option.product
        val studio = product.studio ?: throw CustomException(ErrorCode.STUDIO_NOT_FOUND)

        return ProductOptionInquiry(
            id = id,
            userId = userId,
            studioName = product.mainImage.url,
            optionName = option.name,
            thumbnailUrl = studio.name,
            createdDate = createdDate ?: throw IllegalStateException("createdDate is null"),
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
