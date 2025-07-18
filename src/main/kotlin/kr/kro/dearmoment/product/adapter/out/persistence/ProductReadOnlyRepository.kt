package kr.kro.dearmoment.product.adapter.out.persistence

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions.plus
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions.times
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions.value
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Paths.path
import com.linecorp.kotlinjdsl.render.RenderContext
import com.linecorp.kotlinjdsl.support.hibernate.extension.createQuery
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.like.domain.SortCriteria
import kr.kro.dearmoment.product.adapter.out.jdsl.JdslSortStrategy.toJdslComparator
import kr.kro.dearmoment.product.adapter.out.jdsl.SearchProductOrderByPriceDto
import kr.kro.dearmoment.product.adapter.out.jdsl.inIfNotEmpty
import kr.kro.dearmoment.product.adapter.out.jdsl.joinIfNotEmpty
import kr.kro.dearmoment.product.application.dto.query.SearchProductQuery
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity
import kr.kro.dearmoment.studio.domain.StudioStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
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
    ): Page<Product> {
        val isPriceSort = query.sortBy in listOf(SortCriteria.PRICE_HIGH, SortCriteria.PRICE_LOW)

        // ───────── 1단계: 페이지 단위로 PK 목록 조회 ─────────
        val productIds: List<Long> =
            if (isPriceSort) {
                // 가격 정렬(PRICE_HIGH / PRICE_LOW)
                val idDtos =
                    productJpaRepository.findAll(pageable) {
                        val partnerShopJoin =
                            if (query.partnerShopCategories.isNotEmpty()) {
                                join(ProductOptionEntity::partnerShops)
                            } else {
                                null
                            }

                        selectNew<SearchProductOrderByPriceDto>(
                            path(ProductEntity::productId),
                            when (query.sortBy) {
                                SortCriteria.PRICE_HIGH -> max(path(ProductOptionEntity::discountPrice))
                                SortCriteria.PRICE_LOW -> min(path(ProductOptionEntity::discountPrice))
                                else -> min(path(ProductOptionEntity::discountPrice))
                            },
                        ).from(
                            entity(ProductEntity::class),
                            leftJoin(ProductEntity::options),
                            join(ProductEntity::studio),
                            partnerShopJoin,
                            joinIfNotEmpty(query.availableSeasons) { ProductEntity::availableSeasons },
                            joinIfNotEmpty(query.cameraTypes) { ProductEntity::cameraTypes },
                            joinIfNotEmpty(query.retouchStyles) { ProductEntity::retouchStyles },
                        ).whereAnd(
                            path(ProductEntity::studio)(StudioEntity::status)
                                .eq(StudioStatus.ACTIVE.name),
                            path(ProductOptionEntity::discountPrice)
                                .between(query.minPrice, query.maxPrice),
                            path(PartnerShopEmbeddable::category)
                                .inIfNotEmpty(query.partnerShopCategories),
                            entity(ShootingSeason::class).inIfNotEmpty(query.availableSeasons),
                            entity(CameraType::class).inIfNotEmpty(query.cameraTypes),
                            entity(RetouchStyle::class).inIfNotEmpty(query.retouchStyles),
                        ).groupBy(path(ProductEntity::productId))
                            .orderBy(query.sortBy.toJdslComparator())
                    }
                idDtos.mapNotNull { it?.productId }
            } else {
                // 인기 정렬(가중치) - 서브쿼리 분리 방식

                // 1-1단계: 조건에 맞는 product_id만 먼저 조회
                val filteredProductIds =
                    productJpaRepository.findAll {
                        val partnerShopJoin =
                            if (query.partnerShopCategories.isNotEmpty()) {
                                join(ProductOptionEntity::partnerShops)
                            } else {
                                null
                            }

                        selectDistinct(path(ProductEntity::productId))
                            .from(
                                entity(ProductEntity::class),
                                leftJoin(ProductEntity::options),
                                join(ProductEntity::studio),
                                partnerShopJoin,
                                joinIfNotEmpty(query.availableSeasons) { ProductEntity::availableSeasons },
                                joinIfNotEmpty(query.cameraTypes) { ProductEntity::cameraTypes },
                                joinIfNotEmpty(query.retouchStyles) { ProductEntity::retouchStyles },
                            ).whereAnd(
                                path(ProductEntity::studio)(StudioEntity::status)
                                    .eq(StudioStatus.ACTIVE.name),
                                path(ProductOptionEntity::discountPrice)
                                    .between(query.minPrice, query.maxPrice),
                                path(PartnerShopEmbeddable::category)
                                    .inIfNotEmpty(query.partnerShopCategories),
                                entity(ShootingSeason::class).inIfNotEmpty(query.availableSeasons),
                                entity(CameraType::class).inIfNotEmpty(query.cameraTypes),
                                entity(RetouchStyle::class).inIfNotEmpty(query.retouchStyles),
                            )
                    }.mapNotNull { it }

                if (filteredProductIds.isEmpty()) {
                    return PageImpl(emptyList(), pageable, 0)
                }

                // 1-2단계: 필터링된 product_id로 가중치 계산 및 정렬
                data class WeightedDto(val productId: Long?, val weight: Long?)

                val weightExpr =
                    plus(
                        plus(
                            times(path(ProductEntity::likeCount), value(10L)),
                            times(path(ProductEntity::inquiryCount), value(12L)),
                        ),
                        times(path(ProductEntity::optionLikeCount), value(11L)),
                    )

                val idDtos =
                    productJpaRepository.findAll(pageable) {
                        selectNew<WeightedDto>(
                            path(ProductEntity::productId),
                            weightExpr
                        ).from(
                            entity(ProductEntity::class)
                        ).where(
                            path(ProductEntity::productId).`in`(filteredProductIds)
                        ).orderBy(weightExpr.desc())
                    }

                idDtos.mapNotNull { it?.productId }
            }

        if (productIds.isEmpty()) return PageImpl(emptyList(), pageable, 0)

        // ───────── 2단계: Fetch‑Join으로 상세 재조회 ─────────
        val products =
            productJpaRepository.findAll {
                selectDistinct(entity(ProductEntity::class))
                    .from(
                        entity(ProductEntity::class),
                        leftJoin(ProductEntity::options),
                        fetchJoin(ProductEntity::studio),
                    ).where(
                        path(ProductEntity::productId).`in`(productIds)
                    )
            }.mapNotNull { it?.toDomain() }

        // ───────── 3단계: 원래 정렬 순서 유지 ─────────
        val sortedProducts =
            if (isPriceSort) {
                // 가격 정렬의 경우 이미 정렬된 순서대로 반환
                val productMap = products.associateBy { it.productId }
                productIds.mapNotNull { productMap[it] }
            } else {
                // 인기 정렬의 경우 가중치 순서대로 반환
                val productMap = products.associateBy { it.productId }
                productIds.mapNotNull { productMap[it] }
            }

        // ───────── 4단계: PageImpl 래핑 후 반환 ─────────
        return PageImpl(sortedProducts, pageable, productIds.size.toLong())
    }

    override fun existsByUserIdAndTitle(
        userId: UUID,
        title: String
    ): Boolean = productJpaRepository.existsByUserIdAndTitle(userId, title)

    override fun findById(id: Long): Product? = productJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun findAll(): List<Product> = productJpaRepository.findAll().map { it.toDomain() }

    override fun findByUserId(userId: UUID): List<Product> = productJpaRepository.findByUserId(userId).map { it.toDomain() }

    override fun existsById(id: Long): Boolean = productJpaRepository.existsById(id)

    override fun findWithStudioById(id: Long): Product {
        val query =
            jpql {
                select(entity(ProductEntity::class))
                    .from(entity(ProductEntity::class), fetchJoin(ProductEntity::studio))
                    .where(path(ProductEntity::productId).eq(id))
            }

        val product =
            entityManager
                .createQuery(query, jpqlRenderContext)
                .resultList
                .firstOrNull() ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)

        return product.toDomain()
    }
}
