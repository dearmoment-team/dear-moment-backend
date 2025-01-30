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
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

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
    var optionId: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    var product: ProductEntity? = null,
    @Column(name = "NAME", nullable = false)
    var name: String = "",
    @Column(name = "ADDITIONAL_PRICE", nullable = false)
    var additionalPrice: Long = 0L,
    @Column(name = "DESCRIPTION")
    var description: String? = null,
    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false)
    var createdAt: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    var updatedAt: LocalDateTime? = null,
) {
    companion object {
        fun fromDomain(
            option: ProductOption,
            productEntity: ProductEntity,
        ): ProductOptionEntity {
            return ProductOptionEntity(
                optionId = option.optionId,
                product = productEntity,
                name = option.name,
                additionalPrice = option.additionalPrice,
                description = option.description,
                createdAt = option.createdAt ?: LocalDateTime.now(),
                updatedAt = option.updatedAt ?: LocalDateTime.now(),
            )
        }
    }

    fun toDomain(): ProductOption {
        return ProductOption(
            optionId = optionId,
            productId = product?.productId,
            name = name,
            additionalPrice = additionalPrice,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
