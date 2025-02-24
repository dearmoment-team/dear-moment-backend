package kr.kro.dearmoment.product.adapter.out.persistence

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
import kr.kro.dearmoment.product.domain.model.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
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
    @Column(name = "USER_ID")
    var userId: Long? = null,
    @Column(name = "TITLE", nullable = false)
    var title: String = "",
    @Column(name = "DESCRIPTION")
    var description: String? = null,
    @Column(name = "BASE_PRICE", nullable = false)
    var basePrice: Long = 0L,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "PRODUCT_AVAILABLE_SEASONS",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")],
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "SEASON")
    var availableSeasons: MutableSet<ShootingSeason> = mutableSetOf(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "PRODUCT_CAMERA_TYPES",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")],
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "CAMERA_TYPE")
    var cameraTypes: MutableSet<CameraType> = mutableSetOf(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "PRODUCT_RETOUCH_STYLES",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")],
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
        joinColumns = [JoinColumn(name = "PRODUCT_ID")],
    )
    var subImages: MutableList<ImageEmbeddable> = mutableListOf(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "PRODUCT_ADDITIONAL_IMAGES",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")],
    )
    var additionalImages: MutableList<ImageEmbeddable> = mutableListOf(),
    @Column(name = "DETAILED_INFO")
    var detailedInfo: String? = null,
    @Column(name = "CONTACT_INFO")
    var contactInfo: String? = null,
    @CreatedDate
    @Column(name = "CREATED_AT", updatable = false)
    var createdAt: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    var updatedAt: LocalDateTime? = null,
    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    var options: MutableList<ProductOptionEntity> = mutableListOf(),
) {
    companion object {
        fun fromDomain(product: Product): ProductEntity {
            val productEntity =
                ProductEntity(
                    productId = if (product.productId == 0L) null else product.productId,
                    userId = product.userId,
                    title = product.title,
                    description = product.description.takeIf { it.isNotBlank() },
                    basePrice = product.basePrice,
                    mainImage = ImageEmbeddable.fromDomainImage(product.mainImage),
                    detailedInfo = product.detailedInfo.takeIf { it.isNotBlank() },
                    contactInfo = product.contactInfo.takeIf { it.isNotBlank() },
                    createdAt = product.createdAt,
                    updatedAt = product.updatedAt,
                )

            // 다중 선택(availableSeasons, cameraTypes, retouchStyles) 매핑
            productEntity.availableSeasons.addAll(product.availableSeasons)
            productEntity.cameraTypes.addAll(product.cameraTypes)
            productEntity.retouchStyles.addAll(product.retouchStyles)

            // 서브/추가 이미지 매핑
            productEntity.subImages =
                product.subImages
                    .map { ImageEmbeddable.fromDomainImage(it) }
                    .toMutableList()
            productEntity.additionalImages =
                product.additionalImages
                    .map { ImageEmbeddable.fromDomainImage(it) }
                    .toMutableList()

            // 옵션 매핑
            productEntity.options.clear()
            product.options.forEach { optionDomain ->
                val optionEntity = ProductOptionEntity.fromDomain(optionDomain, productEntity)
                productEntity.options.add(optionEntity)
            }

            return productEntity
        }
    }

    fun toDomain(): Product {
        return Product(
            productId = productId ?: 0L,
            userId = userId ?: throw IllegalArgumentException("User ID is null"),
            title = title,
            description = description ?: "",
            basePrice = basePrice,
            availableSeasons = availableSeasons,
            cameraTypes = cameraTypes,
            retouchStyles = retouchStyles,
            mainImage = mainImage.toDomainImage(),
            subImages = subImages.map { it.toDomainImage() },
            additionalImages = additionalImages.map { it.toDomainImage() },
            detailedInfo = detailedInfo ?: "",
            contactInfo = contactInfo ?: "",
            createdAt = createdAt ?: LocalDateTime.now(),
            updatedAt = updatedAt ?: LocalDateTime.now(),
            options = options.map { it.toDomain() },
        )
    }
}
