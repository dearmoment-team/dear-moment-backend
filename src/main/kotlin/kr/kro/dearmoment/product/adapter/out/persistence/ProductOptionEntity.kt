package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.product.domain.model.ProductOption

@Entity
@Table(name = "PRODUCT_OPTIONS")
open class ProductOptionEntity(
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "product_options_seq",
    )
    @SequenceGenerator(
        name = "product_options_seq",
        sequenceName = "PRODUCT_OPTIONS_SEQ",
        allocationSize = 1,
    )
    @Column(name = "OPTION_ID")
    open var optionId: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    open var product: ProductEntity? = null,
    @Column(name = "NAME", nullable = false)
    open var name: String = "",
    @Column(name = "ADDITIONAL_PRICE", nullable = false)
    open var additionalPrice: Long = 0L,
    @Column(name = "DESCRIPTION")
    open var description: String? = null,
    // 낙관적 잠금을 위한 버전 필드 (기본값 0, non-nullable)
    @Version
    @Column(name = "VERSION", nullable = false)
    open var version: Long = 0L,
) : Auditable() {
    companion object {
        fun fromDomain(
            option: ProductOption,
            productEntity: ProductEntity,
        ): ProductOptionEntity {
            val entity =
                ProductOptionEntity(
                    optionId = if (option.optionId == 0L) null else option.optionId,
                    product = productEntity,
                    name = option.name,
                    additionalPrice = option.additionalPrice,
                    description = option.description.takeIf { it.isNotBlank() },
                )
            // 도메인 시간이 있다면 반영
            entity.createdDate = option.createdAt
            entity.updateDate = option.updatedAt
            return entity
        }
    }

    fun toDomain(): ProductOption {
        return ProductOption(
            optionId = optionId ?: 0L,
            productId = product?.productId ?: 0L,
            name = name,
            additionalPrice = additionalPrice,
            description = description ?: "",
            createdAt = this.createdDate,
            updatedAt = this.updateDate,
        )
    }
}
