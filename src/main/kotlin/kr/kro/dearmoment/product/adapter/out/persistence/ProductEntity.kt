package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity

@Entity
@Table(name = "PRODUCTS")
class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_seq")
    @SequenceGenerator(name = "products_seq", sequenceName = "PRODUCTS_SEQ", allocationSize = 1)
    @Column(name = "PRODUCT_ID")
    var productId: Long? = null,
    @Column(name = "USER_ID", nullable = false)
    var userId: Long,
    @Column(name = "PRODUCT_TYPE", nullable = false)
    var productType: String,
    @Column(name = "SHOOTING_PLACE", nullable = false)
    var shootingPlace: String,
    @Column(name = "TITLE", nullable = false)
    var title: String = "",
    @Column(name = "DESCRIPTION")
    var description: String = "",
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PRODUCT_AVAILABLE_SEASONS", joinColumns = [JoinColumn(name = "PRODUCT_ID")])
    @Column(name = "SEASON")
    var availableSeasons: MutableSet<String> = mutableSetOf(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PRODUCT_CAMERA_TYPES", joinColumns = [JoinColumn(name = "PRODUCT_ID")])
    @Column(name = "CAMERA_TYPE")
    var cameraTypes: MutableSet<String> = mutableSetOf(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PRODUCT_RETOUCH_STYLES", joinColumns = [JoinColumn(name = "PRODUCT_ID")])
    @Column(name = "RETOUCH_STYLE")
    var retouchStyles: MutableSet<String> = mutableSetOf(),
    @Embedded
    @AttributeOverrides(AttributeOverride(name = "userId", column = Column(name = "MAIN_IMAGE_USER_ID")))
    var mainImage: ImageEmbeddable = ImageEmbeddable(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PRODUCT_SUB_IMAGES", joinColumns = [JoinColumn(name = "PRODUCT_ID")])
    var subImages: MutableList<ImageEmbeddable> = mutableListOf(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PRODUCT_ADDITIONAL_IMAGES", joinColumns = [JoinColumn(name = "PRODUCT_ID")])
    var additionalImages: MutableList<ImageEmbeddable> = mutableListOf(),
    @Column(name = "DETAILED_INFO")
    var detailedInfo: String = "",
    @Column(name = "CONTACT_INFO")
    var contactInfo: String = "",
    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var options: MutableList<ProductOptionEntity> = mutableListOf(),
    @Column(nullable = false)
    var version: Long = 0L,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studio_id")
    var studio: StudioEntity,
) : Auditable() {
    @Column(nullable = false)
    val likeCount: Long = 0

    @Column(nullable = false)
    val inquiryCount: Long = 0

    companion object {
        fun fromDomain(
            product: Product,
            studio: StudioEntity,
        ): ProductEntity {
            val productEntity =
                ProductEntity(
                    productId = if (product.productId == 0L) null else product.productId,
                    userId = product.userId,
                    productType = product.productType.name,
                    shootingPlace = product.shootingPlace.name,
                    title = product.title,
                    description = product.description.takeIf { it.isNotBlank() } ?: "",
                    mainImage = ImageEmbeddable.fromDomainImage(product.mainImage),
                    detailedInfo = product.detailedInfo.takeIf { it.isNotBlank() } ?: "",
                    contactInfo = product.contactInfo.takeIf { it.isNotBlank() } ?: "",
                    studio = studio,
                )
            productEntity.availableSeasons = product.availableSeasons.map { it.name }.toMutableSet()
            productEntity.cameraTypes = product.cameraTypes.map { it.name }.toMutableSet()
            productEntity.retouchStyles = product.retouchStyles.map { it.name }.toMutableSet()
            productEntity.subImages = product.subImages.map { ImageEmbeddable.fromDomainImage(it) }.toMutableList()
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
            userId = userId,
            productType = ProductType.from(productType),
            shootingPlace = ShootingPlace.from(shootingPlace),
            title = title,
            description = description,
            availableSeasons = availableSeasons.map { ShootingSeason.from(it) }.toSet(),
            cameraTypes = cameraTypes.map { CameraType.from(it) }.toSet(),
            retouchStyles = retouchStyles.map { RetouchStyle.from(it) }.toSet(),
            mainImage = mainImage.toDomainImage(),
            subImages = subImages.map { it.toDomainImage() },
            additionalImages = additionalImages.map { it.toDomainImage() },
            detailedInfo = detailedInfo,
            contactInfo = contactInfo,
            options = options.map { it.toDomain() },
            studio = studio.toDomain(),
            likeCount = likeCount,
            inquiryCount = inquiryCount,
        )
    }
}
