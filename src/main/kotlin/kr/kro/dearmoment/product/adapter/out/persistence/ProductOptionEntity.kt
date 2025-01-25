package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.*
import kr.kro.dearmoment.product.domain.model.ProductOption
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
            domain: ProductOption,
            productEntity: ProductEntity
        ): ProductOptionEntity {
            require(!domain.name.isNullOrBlank()) { "ProductOption name must not be null or blank" }
            require(domain.additionalPrice >= 0) { "ProductOption additionalPrice must not be negative" }

            return ProductOptionEntity(
                optionId = if (domain.optionId == 0L) null else domain.optionId,
                name = domain.name ?: "기본 옵션 이름", // 기본값 설정
                additionalPrice = domain.additionalPrice,
                description = domain.description,
                product = productEntity,
                createdAt = domain.createdAt ?: LocalDateTime.now(), // 기본값 설정
                updatedAt = domain.updatedAt ?: LocalDateTime.now()  // 기본값 설정
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
