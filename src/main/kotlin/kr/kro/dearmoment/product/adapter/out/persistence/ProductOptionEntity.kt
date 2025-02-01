package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "PRODUCT_OPTIONS")
@EntityListeners(AuditingEntityListener::class)
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
        /**
         * 도메인 모델의 ProductOption을 엔티티로 변환합니다.
         * 신규 옵션의 경우, 도메인에서 optionId가 0L이면 엔티티에서는 null로 변환하여 ID 자동 생성이 되도록 합니다.
         */
        fun fromDomain(
            option: ProductOption,
            productEntity: ProductEntity,
        ): ProductOptionEntity {
            return ProductOptionEntity(
                optionId = if (option.optionId == 0L) null else option.optionId,
                product = productEntity,
                name = option.name,
                additionalPrice = option.additionalPrice,
                description = option.description.takeIf { it.isNotBlank() },
                createdAt = null,
                updatedAt = null,
            )
        }
    }

    /**
     * 엔티티를 도메인 모델의 ProductOption으로 변환합니다.
     * product가 null인 경우, productId는 0L로 처리합니다.
     * 여기서는 createdAt과 updatedAt이 null이면 그대로 null을 전달합니다.
     */
    fun toDomain(): ProductOption {
        return ProductOption(
            optionId = optionId ?: 0L,
            productId = product?.productId ?: 0L,
            name = name,
            additionalPrice = additionalPrice,
            description = description ?: "",
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
