package kr.kro.dearmoment.product.adapter.out.persistence

import com.linecorp.kotlinjdsl.render.RenderContext
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.product.application.dto.query.SearchProductQuery
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ProductReadOnlyRepository(
    private val productJpaRepository: JpaProductRepository,
    private val entityManager: EntityManager,
    private val jpqlRenderContext: RenderContext,
) : GetProductPort {
    override fun searchByCriteria(
        title: String?,
        productType: String?,
        shootingPlace: String?,
        sortBy: String?,
    ): List<Product> {
        val pt: ProductType? = productType?.let { ProductType.valueOf(it) }
        val sp: ShootingPlace? = shootingPlace?.let { ShootingPlace.valueOf(it) }
        val productEntities = productJpaRepository.searchByCriteria(title, pt, sp, sortBy)
        return productEntities.map { it.toDomain() }
    }

    override fun searchByCriteria2(
        query: SearchProductQuery,
        pageable: Pageable,
    ): Page<Product> {
        return productJpaRepository.findPage(pageable) {
            select(
                entity(ProductEntity::class),
            ).from(
                entity(ProductEntity::class),
                fetchJoin(ProductEntity::options)
                    .on(path(ProductOptionEntity::originalPrice).between(query.minPrice, query.maxPrice)),
                fetchJoin(ProductEntity::studio),
                join(ProductOptionEntity::partnerShops)
                    .on(path(PartnerShopEmbeddable::category).`in`(query.partnerShopCategories)),
            ).where(
                and(
                    path(ProductEntity::availableSeasons).`in`(query.availableSeasons),
                    path(ProductEntity::cameraTypes).`in`(query.cameraTypes),
                    path(ProductEntity::retouchStyles).`in`(query.retouchStyles),
                ),
            )
        }.map { it?.toDomain() }
    }

    override fun existsByUserIdAndTitle(
        userId: Long,
        title: String,
    ): Boolean = productJpaRepository.existsByUserIdAndTitle(userId, title)

    override fun findById(id: Long): Product? {
        return productJpaRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun findAll(): List<Product> {
        return productJpaRepository.findAll().map { it.toDomain() }
    }

    override fun findByUserId(userId: Long): List<Product> {
        return productJpaRepository.findByUserId(userId).map { it.toDomain() }
    }

    override fun existsById(id: Long): Boolean {
        return productJpaRepository.existsById(id)
    }
}
