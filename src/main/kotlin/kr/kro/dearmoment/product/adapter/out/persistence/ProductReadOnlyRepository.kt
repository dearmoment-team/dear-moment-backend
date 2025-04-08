package kr.kro.dearmoment.product.adapter.out.persistence

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.RenderContext
import com.linecorp.kotlinjdsl.support.hibernate.extension.createQuery
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.product.adapter.out.persistence.sort.SortCriteria
import kr.kro.dearmoment.product.application.dto.query.SearchProductQuery
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity
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
    ): List<Product> =
        productJpaRepository.findAll(pageable) {
            val inPartnerShopsPredicate =
                if (query.partnerShopCategories.isNotEmpty()) {
                    path(
                        PartnerShopEmbeddable::category,
                    ).`in`(query.partnerShopCategories)
                } else {
                    null
                }
            val inAvailableSeasonsPredicate =
                if (query.availableSeasons.isNotEmpty()) entity(ShootingSeason::class).`in`(query.availableSeasons) else null
            val inCameraTypesPredicate =
                if (query.cameraTypes.isNotEmpty()) entity(CameraType::class).`in`(query.cameraTypes) else null
            val inRetouchStylesPredicate =
                if (query.retouchStyles.isNotEmpty()) entity(RetouchStyle::class).`in`(query.retouchStyles) else null

            select(
                entity(ProductEntity::class),
            ).from(
                entity(ProductEntity::class),
                leftJoin(ProductEntity::options),
                fetchJoin(ProductEntity::studio),
                if (query.partnerShopCategories.isNotEmpty()) join(ProductOptionEntity::partnerShops) else null,
                if (query.availableSeasons.isNotEmpty()) join(ProductEntity::availableSeasons) else null,
                if (query.cameraTypes.isNotEmpty()) join(ProductEntity::cameraTypes) else null,
                if (query.retouchStyles.isNotEmpty()) join(ProductEntity::retouchStyles) else null,
            ).whereAnd(
                path(ProductEntity::studio)(StudioEntity::status).eq("ACTIVE"),
                path(ProductOptionEntity::discountPrice).between(query.minPrice, query.maxPrice),
                inPartnerShopsPredicate,
                inAvailableSeasonsPredicate,
                inCameraTypesPredicate,
                inRetouchStylesPredicate,
            ).orderBy(query.sortBy.strategy)
        }.mapNotNull { it?.toDomain() }

    override fun searchByCriteriaOrderByPrice(
        query: SearchProductQuery,
        pageable: Pageable,
    ): List<Product> =
        productJpaRepository.findAll(pageable) {
            val inPartnerShopsPredicate =
                if (query.partnerShopCategories.isNotEmpty()) {
                    path(
                        PartnerShopEmbeddable::category,
                    ).`in`(query.partnerShopCategories)
                } else {
                    null
                }
            val inAvailableSeasonsPredicate =
                if (query.availableSeasons.isNotEmpty()) entity(ShootingSeason::class).`in`(query.availableSeasons) else null
            val inCameraTypesPredicate =
                if (query.cameraTypes.isNotEmpty()) entity(CameraType::class).`in`(query.cameraTypes) else null
            val inRetouchStylesPredicate =
                if (query.retouchStyles.isNotEmpty()) entity(RetouchStyle::class).`in`(query.retouchStyles) else null

            selectNew<SearchProductResponse2>(
                entity(ProductEntity::class),
                if (query.sortBy == SortCriteria.PRICE_HIGH) {
                    max(path(ProductOptionEntity::discountPrice))
                } else {
                    min(path(ProductOptionEntity::discountPrice))
                }
            ).from(
                entity(ProductEntity::class),
                leftJoin(ProductEntity::options),
                fetchJoin(ProductEntity::studio),
                if (query.partnerShopCategories.isNotEmpty()) join(ProductOptionEntity::partnerShops) else null,
                if (query.availableSeasons.isNotEmpty()) join(ProductEntity::availableSeasons) else null,
                if (query.cameraTypes.isNotEmpty()) join(ProductEntity::cameraTypes) else null,
                if (query.retouchStyles.isNotEmpty()) join(ProductEntity::retouchStyles) else null,
            ).whereAnd(
                path(ProductEntity::studio)(StudioEntity::status).eq("ACTIVE"),
                path(ProductOptionEntity::discountPrice).between(query.minPrice, query.maxPrice),
                inPartnerShopsPredicate,
                inAvailableSeasonsPredicate,
                inCameraTypesPredicate,
                inRetouchStylesPredicate,
            ).groupBy(
                path(ProductEntity::productId)
            ).orderBy(
                query.sortBy.strategy
            )
        }.mapNotNull { it?.productEntity?.toDomain() }

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

data class SearchProductResponse2(
    val productEntity: ProductEntity,
    val priceSum: Long,
)
