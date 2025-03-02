package kr.kro.dearmoment.product.adapter.out.persistence

import Auditable
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
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
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "PRODUCTS")
@EntityListeners(AuditingEntityListener::class)
open class ProductEntity(
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "products_seq",
    )
    @SequenceGenerator(
        name = "products_seq",
        sequenceName = "PRODUCTS_SEQ",
        allocationSize = 1,
    )
    @Column(name = "PRODUCT_ID")
    var productId: Long? = null,

    @Column(name = "USER_ID", nullable = false)
    var userId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "PRODUCT_TYPE", nullable = false)
    var productType: ProductType? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "SHOOTING_PLACE", nullable = false)
    var shootingPlace: ShootingPlace? = null,

    @Column(name = "TITLE", nullable = false)
    var title: String = "",

    @Column(name = "DESCRIPTION")
    var description: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "PRODUCT_AVAILABLE_SEASONS",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "SEASON")
    var availableSeasons: MutableSet<ShootingSeason> = mutableSetOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "PRODUCT_CAMERA_TYPES",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "CAMERA_TYPE")
    var cameraTypes: MutableSet<CameraType> = mutableSetOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "PRODUCT_RETOUCH_STYLES",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "RETOUCH_STYLE")
    var retouchStyles: MutableSet<RetouchStyle> = mutableSetOf(),

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "userId", column = Column(name = "MAIN_IMAGE_USER_ID"))
    )
    var mainImage: ImageEmbeddable = ImageEmbeddable(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "PRODUCT_SUB_IMAGES",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")]
    )
    var subImages: MutableList<ImageEmbeddable> = mutableListOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "PRODUCT_ADDITIONAL_IMAGES",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")]
    )
    var additionalImages: MutableList<ImageEmbeddable> = mutableListOf(),

    @Column(name = "DETAILED_INFO")
    var detailedInfo: String? = null,

    @Column(name = "CONTACT_INFO")
    var contactInfo: String? = null,

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    var options: MutableList<ProductOptionEntity> = mutableListOf(),

    @Column(nullable = false)
    open var version: Long = 0L,
) : Auditable() {
    companion object {
        fun fromDomain(product: Product): ProductEntity {
            val productEntity = ProductEntity(
                productId = if (product.productId == 0L) null else product.productId,
                userId = product.userId,
                productType = product.productType,
                shootingPlace = product.shootingPlace,
                title = product.title,
                description = product.description.takeIf { it.isNotBlank() },
                mainImage = ImageEmbeddable.fromDomainImage(product.mainImage),
                detailedInfo = product.detailedInfo.takeIf { it.isNotBlank() },
                contactInfo = product.contactInfo.takeIf { it.isNotBlank() },
            )
            productEntity.availableSeasons.addAll(product.availableSeasons)
            productEntity.cameraTypes.addAll(product.cameraTypes)
            productEntity.retouchStyles.addAll(product.retouchStyles)
            productEntity.subImages =
                product.subImages.map { ImageEmbeddable.fromDomainImage(it) }.toMutableList()
            productEntity.additionalImages =
                product.additionalImages.map { ImageEmbeddable.fromDomainImage(it) }.toMutableList()
            productEntity.options.clear()
            product.options.forEach { opt ->
                val optionEntity = ProductOptionEntity.fromDomain(opt, productEntity)
                productEntity.options.add(optionEntity)
            }
            return productEntity
        }
    }

    fun toDomain(): Product {
        return Product(
            productId = productId ?: 0L,
            userId = userId ?: throw IllegalArgumentException("User ID is null"),
            productType = productType ?: throw IllegalArgumentException("ProductType is null"),
            shootingPlace = shootingPlace ?: throw IllegalArgumentException("ShootingPlace is null"),
            title = title,
            description = description ?: "",
            availableSeasons = availableSeasons,
            cameraTypes = cameraTypes,
            retouchStyles = retouchStyles,
            mainImage = mainImage.toDomainImage(),
            subImages = subImages.map { it.toDomainImage() },
            additionalImages = additionalImages.map { it.toDomainImage() },
            detailedInfo = detailedInfo ?: "",
            contactInfo = contactInfo ?: "",
            options = options.map { it.toDomain() }
        )
    }
}
