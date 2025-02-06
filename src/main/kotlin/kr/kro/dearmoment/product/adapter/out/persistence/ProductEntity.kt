package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.CascadeType
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
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.product.domain.model.ConceptType
import kr.kro.dearmoment.product.domain.model.OriginalProvideType
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.SeasonHalf
import java.time.LocalDateTime

/**
 * Kotlin + Hibernate 환경에서 지연 로딩(proxy) 시 문제가 생기지 않도록
 * 클래스와 프로퍼티에 직접 open을 명시하는 예시.
 */
@Entity
@Table(name = "PRODUCTS")
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
    open var productId: Long? = null,
    @Column(name = "USER_ID")
    open var userId: Long? = null,
    @Column(name = "TITLE", nullable = false)
    open var title: String = "",
    @Column(name = "DESCRIPTION")
    open var description: String? = null,
    @Column(name = "PRICE", nullable = false)
    open var price: Long = 0L,
    /**
     * 0=일반, 1=패키지 등 구분
     */
    @Column(name = "TYPE_CODE", nullable = false)
    open var typeCode: Int = 0,
    @Column(name = "SHOOTING_TIME")
    open var shootingTime: LocalDateTime? = null,
    @Column(name = "SHOOTING_LOCATION")
    open var shootingLocation: String? = null,
    /**
     * 최대 의상 벌 수
     */
    @Column(name = "NUMBER_OF_COSTUMES")
    open var numberOfCostumes: Int? = null,
    /**
     * 우아한/빈티지 등 콘셉트
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "CONCEPT", nullable = false)
    open var concept: ConceptType = ConceptType.ELEGANT,
    /**
     * 원본 제공 방식 (전체 / 일부)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ORIGINAL_PROVIDE_TYPE", nullable = false)
    open var originalProvideType: OriginalProvideType = OriginalProvideType.FULL,
    /**
     * PARTIAL일 때 제공할 원본 장수
     */
    @Column(name = "PARTIAL_ORIGINAL_COUNT")
    open var partialOriginalCount: Int? = null,
    /**
     * 25년 등 연도
     */
    @Column(name = "SEASON_YEAR")
    open var seasonYear: Int? = null,
    /**
     * 상반기/하반기
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "SEASON_HALF")
    open var seasonHalf: SeasonHalf? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "PRODUCT_PARTNER_SHOPS",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")],
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
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "PRODUCT_IMAGES",
        joinColumns = [JoinColumn(name = "PRODUCT_ID")],
    )
    @Column(name = "IMAGE_URL")
    open var images: List<String> = mutableListOf(),
    // 낙관적 잠금을 위한 버전 필드 (기본값 0, non-nullable)
    @Version
    @Column(name = "VERSION", nullable = false)
    open var version: Long = 0L,
) : Auditable() {
    companion object {
        fun fromDomain(product: Product): ProductEntity {
            val entity =
                ProductEntity(
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
                    images = product.images,
                )

            // Auditable 필드(생성/수정일자) 반영
            entity.createdDate = product.createdAt
            entity.updateDate = product.updatedAt

            // 옵션 매핑
            product.options.forEach { optionDomain ->
                val optionEntity = ProductOptionEntity.fromDomain(optionDomain, entity)
                entity.options.add(optionEntity)
            }

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
            images = images,
        )
    }
}
