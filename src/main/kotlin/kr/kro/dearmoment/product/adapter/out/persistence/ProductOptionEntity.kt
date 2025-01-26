package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.*
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

/**
 * ProductOptionEntity는 제품 옵션 정보를 저장하기 위한 JPA 엔티티 클래스입니다.
 * ProductEntity와 연관되며, 옵션의 다양한 속성을 포함합니다.
 */
@Entity
@Table(name = "product_options")
class ProductOptionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val optionId: Long? = null,
    val name: String,
    val additionalPrice: Long,
    val description: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: ProductEntity,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * 도메인 모델을 JPA 엔티티로 변환합니다.
         * @param domain 변환할 ProductOption 도메인 모델
         * @param productEntity ProductEntity (이미 생성된 엔티티)
         */
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

    /**
     * JPA 엔티티를 도메인 모델로 변환합니다.
     * @return ProductOption 도메인 모델
     */
    fun toDomain(): ProductOption {
        return ProductOption(
            optionId = optionId ?: 0L,
            productId = product.productId ?: 0L,
            name = name,
            additionalPrice = additionalPrice,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
