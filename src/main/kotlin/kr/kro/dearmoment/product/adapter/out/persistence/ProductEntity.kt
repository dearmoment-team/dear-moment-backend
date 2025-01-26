package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.*
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * ProductEntity는 제품 정보를 저장하기 위한 JPA 엔티티 클래스입니다.
 * 데이터베이스 테이블과 매핑되며, 다양한 필드와 연관된 옵션 엔티티를 포함합니다.
 */
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
    val title: String = "",

    @Column
    val description: String? = null,

    @Column(nullable = false)
    val price: Long = 0L,

    @Column(name = "type_code", nullable = false)
    val typeCode: Int = 0,

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

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val options: MutableList<ProductOptionEntity> = mutableListOf(),
) {
    constructor() : this(title = "", price = 0L, typeCode = 0)

    companion object {
        /**
         * 도메인 모델을 엔티티로 변환합니다.
         */
        fun fromDomain(product: Product): ProductEntity {
            val productEntity = ProductEntity(
                productId = if (product.productId == 0L) null else product.productId,
                userId = product.userId,
                title = product.title,
                description = product.description,
                price = product.price,
                typeCode = product.typeCode,
                shootingTime = product.shootingTime,
                shootingLocation = product.shootingLocation,
                numberOfCostumes = product.numberOfCostumes,
                packagePartnerShops = product.packagePartnerShops,
                detailedInfo = product.detailedInfo,
                warrantyInfo = product.warrantyInfo,
                contactInfo = product.contactInfo,
                createdAt = product.createdAt ?: LocalDateTime.now(),
                updatedAt = product.updatedAt ?: LocalDateTime.now()
            )

            productEntity.options.clear()
            product.options.forEach { optionDomain ->
                val optionEntity = ProductOptionEntity.fromDomain(optionDomain, productEntity)
                productEntity.options.add(optionEntity)
            }

            return productEntity
        }
    }

    /**
     * 엔티티를 도메인 모델로 변환합니다.
     */
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
            packagePartnerShops = packagePartnerShops,
            detailedInfo = detailedInfo,
            warrantyInfo = warrantyInfo,
            contactInfo = contactInfo,
            createdAt = createdAt,
            updatedAt = updatedAt,
            options = options.map { it.toDomain() }
        )
    }
}
