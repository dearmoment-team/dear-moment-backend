package kr.kro.dearmoment.like.adapter.output.persistence

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.RenderContext
import com.linecorp.kotlinjdsl.support.hibernate.extension.createQuery
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.like.domain.ProductLike
import kr.kro.dearmoment.like.domain.ProductOptionLike
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class LikeReadOnlyPersistenceAdapter(
    private val productOptionLikeJpaRepository: ProductOptionLikeJpaRepository,
    private val productLikeJpaRepository: ProductLikeJpaRepository,
    private val entityManager: EntityManager,
    private val jpqlRenderContext: RenderContext,
) : GetLikePort {
    override fun findUserProductLikes(
        userId: UUID,
        pageable: Pageable,
    ): Page<ProductLike> {
        return productLikeJpaRepository.findPage(pageable) {
            select(
                entity(ProductLikeEntity::class),
            ).from(
                entity(ProductLikeEntity::class),
                fetchJoin(ProductLikeEntity::product),
            ).where(
                path(ProductLikeEntity::userId).eq(userId),
            )
        }.map { it?.toDomain() }
    }

    override fun findUserProductOptionLikes(
        userId: UUID,
        pageable: Pageable,
    ): Page<ProductOptionLike> {
        return productOptionLikeJpaRepository.findPage(pageable) {
            select(
                entity(ProductOptionLikeEntity::class),
            ).from(
                entity(ProductOptionLikeEntity::class),
                fetchJoin(ProductOptionLikeEntity::option),
            ).where(
                path(ProductOptionLikeEntity::userId).eq(userId),
            )
        }.map { it?.toDomain() }
    }

    override fun existProductLike(
        userId: UUID,
        productId: Long,
    ): Boolean {
        val query =
            jpql {
                select(
                    path(ProductLikeEntity::id),
                ).from(
                    entity(ProductLikeEntity::class),
                ).where(
                    and(
                        path(ProductLikeEntity::userId).eq(userId),
                        path(ProductLikeEntity::product).path(ProductEntity::productId).eq(productId),
                    ),
                )
            }
        val likeId = entityManager.createQuery(query, jpqlRenderContext).resultList

        return likeId.isNotEmpty()
    }

    override fun existProductOptionLike(
        userId: UUID,
        productOptionId: Long,
    ): Boolean {
        val query =
            jpql {
                select(
                    path(ProductOptionLikeEntity::id),
                ).from(
                    entity(ProductOptionLikeEntity::class),
                ).where(
                    and(
                        path(ProductOptionLikeEntity::userId).eq(userId),
                        path(ProductOptionLikeEntity::option).path(ProductOptionEntity::optionId).eq(productOptionId),
                    ),
                )
            }
        val likeId = entityManager.createQuery(query, jpqlRenderContext).resultList

        return likeId.isNotEmpty()
    }

    override fun findOptionLikesByUserIdAndOptionIds(
        userId: UUID,
        productOptionId: List<Long>,
    ): List<ProductOptionLike> {
        val query =
            jpql {
                select(
                    entity(ProductOptionLikeEntity::class),
                ).from(
                    entity(ProductOptionLikeEntity::class),
                    fetchJoin(ProductOptionLikeEntity::option),
                ).whereAnd(
                    path(ProductOptionLikeEntity::userId).eq(userId),
                    path(ProductOptionEntity::optionId).`in`(productOptionId),
                )
            }

        return entityManager.createQuery(query, jpqlRenderContext)
            .resultList
            .map { it.toDomain() }
    }

    override fun findProductLikesByUserIdAndProductIds(
        userId: UUID,
        productIds: List<Long>,
    ): List<ProductLike> {
        val query =
            jpql {
                select(
                    entity(ProductLikeEntity::class),
                ).from(
                    entity(ProductLikeEntity::class),
                    fetchJoin(ProductLikeEntity::product),
                ).whereAnd(
                    path(ProductLikeEntity::userId).eq(userId),
                    path(ProductEntity::productId).`in`(productIds),
                )
            }

        return entityManager.createQuery(query, jpqlRenderContext)
            .resultList
            .map { it.toDomain() }
    }

    override fun findProductLikesByUserIdAndProductId(
        userId: UUID,
        productId: Long,
    ): ProductLike? {
        return productLikeJpaRepository.findByIdAndUserId(productId, userId)?.toDomain()
    }
}
