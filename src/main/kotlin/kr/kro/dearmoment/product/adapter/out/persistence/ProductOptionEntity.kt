package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.product.domain.model.option.OptionType
import kr.kro.dearmoment.product.domain.model.option.PartnerShop
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.product.domain.model.option.ProductOption
import org.hibernate.annotations.ColumnDefault

@Entity
@Table(name = "PRODUCT_OPTIONS")
class ProductOptionEntity(
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "product_options_seq"
    )
    @SequenceGenerator(
        name = "product_options_seq",
        sequenceName = "PRODUCT_OPTIONS_SEQ",
        allocationSize = 1
    )
    @Column(name = "OPTION_ID")
    var optionId: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    var product: ProductEntity,

    @Column(name = "NAME", nullable = false)
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "OPTION_TYPE", nullable = false)
    var optionType: OptionType = OptionType.SINGLE,

    @Column(name = "DISCOUNT_AVAILABLE", nullable = false)
    var discountAvailable: Boolean = false,

    @Column(name = "ORIGINAL_PRICE", nullable = false)
    var originalPrice: Long = 0L,

    @Column(name = "DISCOUNT_PRICE", nullable = false)
    var discountPrice: Long = 0L,

    @Column(name = "DESCRIPTION", nullable = false)
    var description: String = " ",

    @Column(name = "COSTUME_COUNT", nullable = false)
    var costumeCount: Int = 0,

    @Column(name = "SHOOTING_LOCATION_COUNT", nullable = false)
    var shootingLocationCount: Int = 0,

    @Column(name = "SHOOTING_HOURS", nullable = false)
    var shootingHours: Int = 0,

    @Column(name = "SHOOTING_MINUTES", nullable = false)
    var shootingMinutes: Int = 0,

    @Column(name = "RETOUCHED_COUNT", nullable = false)
    var retouchedCount: Int = 0,

    @Column(name = "ORIGINAL_PROVIDED", nullable = false)
    var originalProvided: Boolean = false,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "PRODUCT_PARTNER_SHOPS",
        joinColumns = [JoinColumn(name = "OPTION_ID")]
    )
    var partnerShops: List<PartnerShopEmbeddable> = emptyList(),

    @Column(name = "OPTIONAL_ADDITIONAL_DETAILS", nullable = false)
    var optionalAdditionalDetails: String = " ",

    @Column(nullable = false)
    @ColumnDefault("0")
    var version: Long = 0L
) : Auditable() {

    @PrePersist
    @PreUpdate
    private fun applyDefaultBlanks() {
        if (description.isBlank()) {
            description = " "
        }
        if (optionalAdditionalDetails.isBlank()) {
            optionalAdditionalDetails = " "
        }
    }

    companion object {
        fun fromDomain(
            option: ProductOption,
            productEntity: ProductEntity
        ): ProductOptionEntity {
            return ProductOptionEntity(
                optionId = option.optionId,
                product = productEntity,
                name = option.name,
                optionType = option.optionType,
                discountAvailable = option.discountAvailable,
                originalPrice = option.originalPrice,
                discountPrice = option.discountPrice,
                description = option.description.takeIf { it.isNotBlank() } ?: " ",
                costumeCount = option.costumeCount,
                shootingLocationCount = option.shootingLocationCount,
                shootingHours = option.shootingHours,
                shootingMinutes = option.shootingMinutes,
                retouchedCount = option.retouchedCount,
                originalProvided = option.originalProvided,
                partnerShops = option.partnerShops.map {
                    PartnerShopEmbeddable(
                        category = it.category,
                        name = it.name,
                        link = it.link
                    )
                },
                optionalAdditionalDetails = option.optionalAdditionalDetails.takeIf { it.isNotBlank() } ?: " ",
                version = 0L
            )
        }
    }

    fun toDomain(): ProductOption {
        return ProductOption(
            optionId = optionId,
            productId = product.productId ?: 0L,
            name = name,
            optionType = optionType,
            discountAvailable = discountAvailable,
            originalPrice = originalPrice,
            discountPrice = discountPrice,
            description = description.ifBlank { " " },
            costumeCount = costumeCount,
            shootingLocationCount = shootingLocationCount,
            shootingHours = shootingHours,
            shootingMinutes = shootingMinutes,
            retouchedCount = retouchedCount,
            originalProvided = originalProvided,
            partnerShops = partnerShops.map {
                val category = it.category ?: PartnerShopCategory.ETC
                PartnerShop(
                    category = category,
                    name = it.name,
                    link = it.link
                )
            },
            optionalAdditionalDetails = optionalAdditionalDetails.ifBlank { " " },
            createdAt = createdDate,
            updatedAt = updateDate
        )
    }
}