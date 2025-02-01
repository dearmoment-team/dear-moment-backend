package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
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
    @Column(name = "PRICE", nullable = false)
    var price: Long = 0L,
    @Column(name = "TYPE_CODE", nullable = false)
    var typeCode: Int = 0,
    @Column(name = "SHOOTING_TIME")
    var shootingTime: LocalDateTime? = null,
    @Column(name = "SHOOTING_LOCATION")
    var shootingLocation: String? = null,
    @Column(name = "NUMBER_OF_COSTUMES")
    var numberOfCostumes: Int? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "PRODUCT_PARTNER_SHOPS",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")],
    )
    var partnerShops: List<PartnerShopEmbeddable> = mutableListOf(),
    @Column(name = "DETAILED_INFO")
    var detailedInfo: String? = null,
    @Column(name = "WARRANTY_INFO")
    var warrantyInfo: String? = null,
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
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "PRODUCT_IMAGES",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")],
    )
    @Column(name = "IMAGE_URL")
    var images: List<String> = mutableListOf(),
) {
    companion object {
        fun fromDomain(product: Product): ProductEntity {
            val productEntity =
                ProductEntity(
                    productId = if (product.productId == 0L) null else product.productId,
                    userId = product.userId,
                    title = product.title,
                    description = product.description,
                    price = product.price,
                    typeCode = product.typeCode,
                    shootingTime = product.shootingTime,
                    shootingLocation = product.shootingLocation,
                    numberOfCostumes = product.numberOfCostumes,
                    partnerShops = product.partnerShops.map { PartnerShopEmbeddable(it.name, it.link) },
                    detailedInfo = product.detailedInfo,
                    warrantyInfo = product.warrantyInfo,
                    contactInfo = product.contactInfo,
                    createdAt = product.createdAt,
                    updatedAt = product.updatedAt,
                    images = product.images,
                )

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
            userId = userId,
            title = title,
            description = description,
            price = price,
            typeCode = typeCode,
            shootingTime = shootingTime,
            shootingLocation = shootingLocation,
            numberOfCostumes = numberOfCostumes,
            partnerShops = partnerShops.map { PartnerShop(it.name, it.link) },
            detailedInfo = detailedInfo,
            warrantyInfo = warrantyInfo,
            contactInfo = contactInfo,
            createdAt = createdAt,
            updatedAt = updatedAt,
            options = options.map { it.toDomain() },
            images = images,
        )
    }
}
