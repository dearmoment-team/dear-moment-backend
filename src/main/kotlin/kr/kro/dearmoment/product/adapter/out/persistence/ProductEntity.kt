package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.*
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

    /**
     * 제품의 고유 ID입니다. 기본 키로 사용되며 자동으로 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    val productId: Long? = null,

    /**
     * 해당 제품을 소유한 사용자의 ID입니다.
     */
    @Column(name = "user_id")
    val userId: Long? = null,

    /**
     * 제품의 제목입니다. 필수 필드로 설정되어 있습니다.
     */
    @Column(nullable = false)
    val title: String,

    /**
     * 제품의 설명입니다. 선택적 필드입니다.
     */
    @Column
    val description: String? = null,

    /**
     * 제품의 가격입니다. 필수 필드로 설정되어 있습니다.
     */
    @Column(nullable = false)
    val price: Long,

    /**
     * 제품의 유형을 나타내는 코드입니다. 필수 필드로 설정되어 있습니다.
     */
    @Column(name = "type_code", nullable = false)
    val typeCode: Int,

    /**
     * 제품 촬영 시간입니다. 선택적 필드입니다.
     */
    @Column(name = "shooting_time")
    val shootingTime: LocalDateTime? = null,

    /**
     * 제품 촬영 장소입니다. 선택적 필드입니다.
     */
    @Column(name = "shooting_location")
    val shootingLocation: String? = null,

    /**
     * 의상 개수를 나타냅니다. 선택적 필드입니다.
     */
    @Column(name = "number_of_costumes")
    val numberOfCostumes: Int? = null,

    /**
     * 패키지 파트너 상점 정보를 포함합니다. 선택적 필드입니다.
     */
    @Column(name = "package_partner_shops")
    val packagePartnerShops: String? = null,

    /**
     * 제품의 상세 정보를 포함합니다. 선택적 필드입니다.
     */
    @Column(name = "detailed_info")
    val detailedInfo: String? = null,

    /**
     * 보증 정보를 포함합니다. 선택적 필드입니다.
     */
    @Column(name = "warranty_info")
    val warrantyInfo: String? = null,

    /**
     * 연락처 정보를 포함합니다. 선택적 필드입니다.
     */
    @Column(name = "contact_info")
    val contactInfo: String? = null,

    /**
     * 생성 시각입니다. 변경 불가능한 필드로 설정되어 있습니다.
     */
    @Column(name = "created_at", updatable = false)
    @CreatedDate
    val createdAt: LocalDateTime? = null,

    /**
     * 마지막 수정 시각입니다.
     */
    @Column(name = "updated_at")
    @LastModifiedDate
    val updatedAt: LocalDateTime? = null,

    /**
     * 제품과 연관된 옵션 리스트입니다.
     * 옵션은 cascade 및 orphanRemoval 설정으로 관리됩니다.
     */
    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val options: MutableList<ProductOptionEntity> = mutableListOf(),
) {
    companion object {

        /**
         * 도메인 객체를 ProductEntity로 변환합니다.
         */
        fun fromDomain(product: kr.kro.dearmoment.product.domain.model.Product): ProductEntity {
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
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
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
     * ProductEntity를 도메인 객체로 변환합니다.
     */
    fun toDomain(): kr.kro.dearmoment.product.domain.model.Product {
        return kr.kro.dearmoment.product.domain.model.Product(
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
