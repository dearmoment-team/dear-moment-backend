package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@Table(name = "product_options")
open class ProductOptionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    var optionId: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product: ProductEntity? = null,
    @Column(nullable = false)
    var name: String = "",
    @Column(nullable = false)
    var additionalPrice: Long = 0L,
    @Column
    var description: String? = null,
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "updated_at")
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
