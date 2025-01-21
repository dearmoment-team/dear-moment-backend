package kr.kro.dearmoment.product.adapter.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener::class)
class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    val productId: Long? = null,
    @Column(name = "user_id")
    val userId: Long? = null,
    @Column(nullable = false)
    val title: String,
    @Column
    val description: String? = null,
    @Column(nullable = false)
    val price: Long,
    @Column(name = "type_code", nullable = false)
    val typeCode: Int,
    @Column(name = "shooting_time")
    val shootingTime: LocalDateTime? = null,
    @Column(name = "shooting_location")
    val shootingLocation: String? = null,
    @Column(name = "number_of_costumes")
    val numberOfCostumes: Int? = null,
    @Column(name = "package_partner_shops")
    val packagePartnerShops: String? = null,
    @Column(name = "detailed_info")
    val detailedInfo: String? = null,
    @Column(name = "warranty_info")
    val warrantyInfo: String? = null,
    @Column(name = "contact_info")
    val contactInfo: String? = null,
    @Column(name = "created_at", updatable = false)
    @CreatedDate
    val createdAt: LocalDateTime? = null,
    @Column(name = "updated_at")
    @LastModifiedDate
    val updatedAt: LocalDateTime? = null,
) {
    companion object {
        fun fromDomain(product: kr.kro.dearmoment.product.domain.model.Product): ProductEntity {
            return ProductEntity(
                productId = if (product.productId == 0L) null else product.productId,
                userId = product.userId,
                title = product.title,
                description = product.description,
                price = product.price.toLong(),
                typeCode = product.typeCode,
                shootingTime = product.shootingTime,
                shootingLocation = product.shootingLocation,
                numberOfCostumes = product.numberOfCostumes,
                packagePartnerShops = product.packagePartnerShops,
                detailedInfo = product.detailedInfo,
                warrantyInfo = product.warrantyInfo,
                contactInfo = product.contactInfo,
                createdAt = product.createdAt,
                updatedAt = product.updatedAt,
            )
        }
    }

    fun toDomain(): kr.kro.dearmoment.product.domain.model.Product {
        return kr.kro.dearmoment.product.domain.model.Product(
            productId = productId ?: 0L,
            userId = userId,
            title = title,
            description = description,
            price = price.toInt(),
            typeCode = typeCode,
            shootingTime = shootingTime,
            shootingLocation = shootingLocation,
            numberOfCostumes = numberOfCostumes,
            packagePartnerShops = packagePartnerShops,
            detailedInfo = detailedInfo,
            warrantyInfo = warrantyInfo,
            contactInfo = contactInfo,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
