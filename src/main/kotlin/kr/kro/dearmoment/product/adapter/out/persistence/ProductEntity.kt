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
import jakarta.persistence.Table
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener::class)
open class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    var productId: Long? = null,
    @Column(name = "user_id")
    var userId: Long? = null,
    @Column(nullable = false)
    var title: String = "",
    @Column
    var description: String? = null,
    @Column(nullable = false)
    var price: Long = 0L,
    @Column(name = "type_code", nullable = false)
    var typeCode: Int = 0,
    @Column(name = "shooting_time")
    var shootingTime: LocalDateTime? = null,
    @Column(name = "shooting_location")
    var shootingLocation: String? = null,
    @Column(name = "number_of_costumes")
    var numberOfCostumes: Int? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_partner_shops", joinColumns = [JoinColumn(name = "product_id")])
    var partnerShops: List<PartnerShopEmbeddable> = mutableListOf(),
    @Column(name = "detailed_info")
    var detailedInfo: String? = null,
    @Column(name = "warranty_info")
    var warrantyInfo: String? = null,
    @Column(name = "contact_info")
    var contactInfo: String? = null,
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null,
    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    var options: MutableList<ProductOptionEntity> = mutableListOf(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = [JoinColumn(name = "product_id")])
    @Column(name = "image_url")
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
                    createdAt = product.createdAt ?: LocalDateTime.now(),
                    updatedAt = product.updatedAt ?: LocalDateTime.now(),
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
