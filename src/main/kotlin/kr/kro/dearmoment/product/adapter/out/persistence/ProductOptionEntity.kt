package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "product_options")
@EntityListeners(AuditingEntityListener::class)
class ProductOptionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    val optionId: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val additionalPrice: Long,

    @Column
    val description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: ProductEntity,

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,
) {
    companion object {
        fun fromDomain(
            domain: kr.kro.dearmoment.product.domain.model.ProductOption,
            productEntity: ProductEntity,
        ): ProductOptionEntity {
            return ProductOptionEntity(
                optionId = if (domain.optionId == 0L) null else domain.optionId,
                name = domain.name,
                additionalPrice = domain.additionalPrice,
                description = domain.description,
                product = productEntity,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
            )
        }
    }

    fun toDomain(): kr.kro.dearmoment.product.domain.model.ProductOption {
        return kr.kro.dearmoment.product.domain.model.ProductOption(
            optionId = optionId ?: 0L,
            name = name,
            additionalPrice = additionalPrice,
            description = description,
            productId = product.productId ?: 0L,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
