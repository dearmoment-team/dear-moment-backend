package kr.kro.dearmoment.product.adapter.out.persistence

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.RenderContext
import com.linecorp.kotlinjdsl.support.hibernate.extension.createQuery
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.product.adapter.out.persistence.dto.SearchProductOrderByPriceDto
import kr.kro.dearmoment.product.adapter.out.persistence.jdsl.inIfNotEmpty
import kr.kro.dearmoment.product.adapter.out.persistence.jdsl.joinIfNotEmpty
import kr.kro.dearmoment.product.adapter.out.persistence.sort.SortCriteria
import kr.kro.dearmoment.product.application.dto.query.SearchProductQuery
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity
import kr.kro.dearmoment.studio.domain.StudioStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ProductReadOnlyRepository(
    private val productJpaRepository: JpaProductRepository,
    private val entityManager: EntityManager,
    private val jpqlRenderContext: RenderContext,
) : GetProductPort {
    override fun searchByCriteria(
        query: SearchProductQuery,
        pageable: Pageable,
    ): List<Product> {
        val productIds =
            productJpaRepository.findAll(pageable) {
                selectNew<SearchProductOrderByPriceDto>(
                    path(ProductEntity::productId),
                    when (query.sortBy) {
                        SortCriteria.PRICE_HIGH -> max(path(ProductOptionEntity::discountPrice))
                        SortCriteria.PRICE_LOW -> min(path(ProductOptionEntity::discountPrice))
                        else -> path(ProductOptionEntity::discountPrice)
                    }
                ).from(
                    entity(ProductEntity::class),
                    leftJoin(ProductEntity::options),
                    join(ProductEntity::studio),
                    if (query.partnerShopCategories.isNotEmpty()) join(ProductOptionEntity::partnerShops) else null,
                    joinIfNotEmpty(query.availableSeasons) { ProductEntity::availableSeasons },
                    joinIfNotEmpty(query.cameraTypes) { ProductEntity::cameraTypes },
                    joinIfNotEmpty(query.retouchStyles) { ProductEntity::retouchStyles },
                ).whereAnd(
                    path(ProductEntity::studio)(StudioEntity::status).eq(StudioStatus.ACTIVE.name),
                    path(ProductOptionEntity::discountPrice).between(query.minPrice, query.maxPrice),
                    path(PartnerShopEmbeddable::category).inIfNotEmpty(query.partnerShopCategories),
                    entity(ShootingSeason::class).inIfNotEmpty(query.availableSeasons),
                    entity(CameraType::class).inIfNotEmpty(query.cameraTypes),
                    entity(RetouchStyle::class).inIfNotEmpty(query.retouchStyles),
                ).groupBy(
                    if (query.sortBy == SortCriteria.PRICE_HIGH || query.sortBy == SortCriteria.PRICE_LOW) {
                        path(ProductEntity::productId)
                    } else {
                        null
                    }
                ).orderBy(
                    query.sortBy.strategy
                )
            }.map { it?.productId }

        return productJpaRepository.findAll {
            select(
                entity(ProductEntity::class),
            ).from(
                entity(ProductEntity::class),
                leftJoin(ProductEntity::options),
                fetchJoin(ProductEntity::studio),
            ).where(
                path(ProductEntity::productId).`in`(productIds)
            )
        }.mapNotNull { it?.toDomain() }
    }

    override fun existsByUserIdAndTitle(
        userId: UUID,
        title: String,
    ): Boolean = productJpaRepository.existsByUserIdAndTitle(userId, title)

    override fun findById(id: Long): Product? {
        return productJpaRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun findAll(): List<Product> {
        return productJpaRepository.findAll().map { it.toDomain() }
    }

    override fun findByUserId(userId: UUID): List<Product> {
        return productJpaRepository.findByUserId(userId).map { it.toDomain() }
    }

    override fun existsById(id: Long): Boolean {
        return productJpaRepository.existsById(id)
    }

    override fun findWithStudioById(id: Long): Product {
        val query =
            jpql {
                select(
                    entity(ProductEntity::class),
                ).from(
                    entity(ProductEntity::class),
                    fetchJoin(ProductEntity::studio),
                ).where(
                    path(ProductEntity::productId).eq(id),
                )
            }

        val product =
            entityManager.createQuery(query, jpqlRenderContext).resultList.firstOrNull()
                ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)

        return product.toDomain()
    }
}
