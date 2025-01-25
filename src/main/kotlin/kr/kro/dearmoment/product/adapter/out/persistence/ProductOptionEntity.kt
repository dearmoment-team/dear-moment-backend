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

    /**
     * 옵션의 고유 ID입니다. 기본 키로 사용되며 자동으로 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    val optionId: Long? = null,

    /**
     * 옵션의 이름입니다. 필수 필드로 설정되어 있습니다.
     */
    @Column(nullable = false)
    val name: String,

    /**
     * 옵션의 추가 가격입니다. 필수 필드로 설정되어 있습니다.
     */
    @Column(nullable = false)
    val additionalPrice: Long,

    /**
     * 옵션에 대한 설명입니다. 선택적 필드입니다.
     */
    @Column
    val description: String? = null,

    /**
     * 옵션이 속한 제품을 나타냅니다. ProductEntity와 다대일 관계로 설정되어 있습니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: ProductEntity,

    /**
     * 생성 시각입니다. 변경 불가능한 필드로 설정되어 있습니다.
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime? = null,

    /**
     * 마지막 수정 시각입니다.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,
) {
    companion object {

        /**
         * 도메인 객체를 ProductOptionEntity로 변환합니다.
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
     * ProductOptionEntity를 도메인 객체로 변환합니다.
     */
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