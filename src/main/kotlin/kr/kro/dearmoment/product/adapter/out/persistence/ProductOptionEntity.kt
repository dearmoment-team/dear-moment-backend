package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.*
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * ProductOptionEntity는 제품 옵션 정보를 저장하기 위한 JPA 엔티티 클래스입니다.
 * ProductEntity와 연관되며, 옵션의 다양한 속성을 포함합니다.
 */
@Entity
@Table(name = "product_options")
@EntityListeners(AuditingEntityListener::class)
class ProductOptionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    val optionId: Long? = null,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = false)
    val additionalPrice: Long = 0L,

    @Column
    val description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity? = null,

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
            return ProductOptionEntity(
                optionId = if (domain.optionId == 0L) null else domain.optionId,
                name = domain.name,
                additionalPrice = domain.additionalPrice,
                description = domain.description,
                product = productEntity,
                createdAt = domain.createdAt ?: LocalDateTime.now(),
                updatedAt = domain.updatedAt ?: LocalDateTime.now()
            )
        }
    }

    fun toDomain(): ProductOption {
        return ProductOption(
            optionId = optionId ?: 0L,
            name = name,
            additionalPrice = additionalPrice,
            description = description,
            productId = product?.productId ?: 0L,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
