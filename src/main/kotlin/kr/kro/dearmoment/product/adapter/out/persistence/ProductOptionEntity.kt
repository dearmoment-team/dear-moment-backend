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
import kr.kro.dearmoment.common.persistence.BaseTime
import kr.kro.dearmoment.product.domain.model.ProductOption
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

    ) : BaseTime() { // BaseTime 상속

    companion object {
        /**
         * 도메인 모델의 ProductOption을 엔티티로 변환합니다.
         * 신규 옵션의 경우, 도메인에서 optionId가 0L이면 엔티티에서는 null로 변환하여 ID 자동 생성이 되도록 합니다.
         */
        fun fromDomain(
            option: ProductOption,
            productEntity: ProductEntity,
        ): ProductOptionEntity {
            val optionEntity = ProductOptionEntity(
                optionId = if (option.optionId == 0L) null else option.optionId,
                product = productEntity,
                name = option.name,
                additionalPrice = option.additionalPrice,
                description = option.description.takeIf { it.isNotBlank() },
            )

            // 도메인에서 넘어온 생성/수정 시간 정보를 엔티티(BaseTime) 필드에 대입해주고 싶다면:
            optionEntity.createdDate = option.createdAt ?: LocalDateTime.now()
            optionEntity.updateDate = option.updatedAt ?: LocalDateTime.now()

            return optionEntity
        }
    }

    /**
     * 엔티티를 도메인 모델의 ProductOption으로 변환합니다.
     * product가 null인 경우, productId는 0L로 처리합니다.
     * BaseTime으로부터 관리되는 필드(createdDate, updateDate)를 도메인 필드로 넘겨줍니다.
     */
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
