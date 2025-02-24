package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.kro.dearmoment.product.domain.model.OptionType
import kr.kro.dearmoment.product.domain.model.PackageCategory
import kr.kro.dearmoment.product.domain.model.PartnerShop
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
    @Enumerated(EnumType.STRING)
    @Column(name = "OPTION_TYPE", nullable = false)
    var optionType: OptionType = OptionType.SINGLE,
    @Column(name = "ADDITIONAL_PRICE", nullable = false)
    var additionalPrice: Long = 0L,
    @Column(name = "DESCRIPTION")
    var description: String? = null,
    // [단품] 필드
    @Column(name = "COSTUME_COUNT")
    var costumeCount: Int = 0,
    @Column(name = "SHOOTING_LOCATION")
    var shootingLocation: String = "",
    @Column(name = "SHOOTING_MINUTES")
    var shootingMinutes: Int = 0,
    // [패키지] 필드
    @Enumerated(EnumType.STRING)
    @Column(name = "PACKAGE_CATEGORY")
    var packageCategory: PackageCategory? = null,
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "PRODUCT_PARTNER_SHOPS",
        joinColumns = [JoinColumn(name = "OPTION_ID")],
    )
    var partnerShops: List<PartnerShopEmbeddable> = emptyList(),
    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false)
    var createdAt: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    var updatedAt: LocalDateTime? = null,
) {
    companion object {
        /**
         * 도메인 모델(ProductOption)을 엔티티로 변환
         */
        fun fromDomain(
            option: ProductOption,
            productEntity: ProductEntity,
        ): ProductOptionEntity {
            return ProductOptionEntity(
                optionId = if (option.optionId == 0L) null else option.optionId,
                product = productEntity,
                name = option.name,
                optionType = option.optionType,
                additionalPrice = option.additionalPrice,
                description = option.description.takeIf { it.isNotBlank() },
                costumeCount = option.costumeCount,
                shootingLocation = option.shootingLocation,
                shootingMinutes = option.shootingMinutes,
                packageCategory = option.packageCategory,
                partnerShops = option.partnerShops.map { PartnerShopEmbeddable(it.name, it.link) },
                createdAt = null,
                updatedAt = null,
            )
        }
    }

    /**
     * 엔티티를 도메인 모델(ProductOption)로 변환
     */
    fun toDomain(): ProductOption {
        return ProductOption(
            optionId = optionId ?: 0L,
            productId = product?.productId ?: 0L,
            name = name,
            optionType = optionType,
            additionalPrice = additionalPrice,
            description = description ?: "",
            costumeCount = costumeCount,
            shootingLocation = shootingLocation,
            shootingMinutes = shootingMinutes,
            packageCategory = packageCategory,
            partnerShops = partnerShops.map { PartnerShop(it.name, it.link) },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
