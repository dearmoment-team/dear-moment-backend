package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.product.application.dto.query.SearchProductQuery
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ProductReadOnlyRepository(
    private val productJpaRepository: JpaProductRepository,
) : GetProductPort {
    override fun searchByCriteria(
        query: SearchProductQuery,
        pageable: Pageable,
    ): Page<Product> {
        return productJpaRepository.findPage(pageable) {
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
            ).where(
                and(
                    path(ProductOptionEntity::discountPrice).between(query.minPrice, query.maxPrice),
                    inPartnerShopsPredicate,
                    inAvailableSeasonsPredicate,
                    inCameraTypesPredicate,
                    inRetouchStylesPredicate,
                ),
            ).orderBy(query.sortBy.strategy)
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
