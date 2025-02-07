package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.CascadeType
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
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.image.adapter.output.persistence.ImageEntity
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.domain.model.ConceptType
import kr.kro.dearmoment.product.domain.model.OriginalProvideType
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.SeasonHalf
import java.time.LocalDateTime

@Entity
@Table(name = "PRODUCTS")
open class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_seq")
    @SequenceGenerator(name = "products_seq", sequenceName = "PRODUCTS_SEQ", allocationSize = 1)
    @Column(name = "PRODUCT_ID")
    open var productId: Long? = null,
    @Column(name = "USER_ID")
    open var userId: Long? = null,
    @Column(name = "TITLE", nullable = false)
    open var title: String = "",
    @Column(name = "DESCRIPTION")
    open var description: String? = null,
    @Column(name = "PRICE", nullable = false)
    open var price: Long = 0L,
    @Column(name = "TYPE_CODE", nullable = false)
    open var typeCode: Int = 0,
    @Column(name = "SHOOTING_TIME")
    open var shootingTime: LocalDateTime? = null,
    @Column(name = "SHOOTING_LOCATION")
    open var shootingLocation: String? = null,
    @Column(name = "NUMBER_OF_COSTUMES")
    open var numberOfCostumes: Int? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "CONCEPT", nullable = false)
    open var concept: ConceptType = ConceptType.ELEGANT,
    @Enumerated(EnumType.STRING)
    @Column(name = "ORIGINAL_PROVIDE_TYPE", nullable = false)
    open var originalProvideType: OriginalProvideType = OriginalProvideType.FULL,
    @Column(name = "PARTIAL_ORIGINAL_COUNT")
    open var partialOriginalCount: Int? = null,
    @Column(name = "SEASON_YEAR")
    open var seasonYear: Int? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "SEASON_HALF")
    open var seasonHalf: SeasonHalf? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @jakarta.persistence.CollectionTable(
        name = "PRODUCT_PARTNER_SHOPS",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")]
    )
    open var partnerShops: List<PartnerShopEmbeddable> = mutableListOf(),
    @Column(name = "DETAILED_INFO")
    open var detailedInfo: String? = null,
    @Column(name = "WARRANTY_INFO")
    open var warrantyInfo: String? = null,
    @Column(name = "CONTACT_INFO")
    open var contactInfo: String? = null,
    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    open var options: MutableList<ProductOptionEntity> = mutableListOf(),
    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    open var images: MutableList<ImageEntity> = mutableListOf(),
    @Version
    @Column(name = "VERSION", nullable = false)
    open var version: Long = 0L,
) : Auditable() {
    companion object {
        fun fromDomain(product: Product): ProductEntity {
            val entity = ProductEntity(
                productId = if (product.productId == 0L) null else product.productId,
                userId = product.userId,
                title = product.title,
                description = if (product.description.isBlank()) null else product.description,
                price = product.price,
                typeCode = product.typeCode,
                shootingTime = product.shootingTime,
                shootingLocation = product.shootingLocation.ifBlank { null },
                numberOfCostumes = if (product.numberOfCostumes == 0) null else product.numberOfCostumes,
                concept = product.concept,
                originalProvideType = product.originalProvideType,
                partialOriginalCount = product.partialOriginalCount,
                seasonYear = product.seasonYear,
                seasonHalf = product.seasonHalf,
                partnerShops = product.partnerShops.map { PartnerShopEmbeddable(it.name, it.link) },
                detailedInfo = if (product.detailedInfo.isBlank()) null else product.detailedInfo,
                warrantyInfo = if (product.warrantyInfo.isBlank()) null else product.warrantyInfo,
                contactInfo = if (product.contactInfo.isBlank()) null else product.contactInfo,
                images = mutableListOf()
            )
            entity.createdDate = product.createdAt
            entity.updateDate = product.updatedAt
            product.options.forEach { optionDomain ->
                val optionEntity = ProductOptionEntity.fromDomain(optionDomain, entity)
                entity.options.add(optionEntity)
            }
            entity.images = product.images.map { fileName ->
                ImageEntity.from(
                    Image(
                        userId = product.userId,
                        fileName = fileName
                    )
                ).apply { this.product = entity }
            }.toMutableList()
            return entity
        }
    }

    fun toDomain(): Product {
        return Product(
            productId = productId ?: 0L,
            userId = userId ?: throw IllegalArgumentException("User ID is null"),
            title = title,
            description = description ?: "",
            price = price,
            typeCode = typeCode,
            concept = concept,
            originalProvideType = originalProvideType,
            partialOriginalCount = partialOriginalCount,
            shootingTime = shootingTime,
            shootingLocation = shootingLocation ?: "",
            numberOfCostumes = numberOfCostumes ?: 0,
            seasonYear = seasonYear,
            seasonHalf = seasonHalf,
            partnerShops = partnerShops.map { PartnerShop(it.name, it.link) },
            detailedInfo = detailedInfo ?: "",
            warrantyInfo = warrantyInfo ?: "",
            contactInfo = contactInfo ?: "",
            createdAt = createdDate,
            updatedAt = updateDate,
            options = options.map { it.toDomain() },
            images = images.map { it.fileName }
        )
    }
}
