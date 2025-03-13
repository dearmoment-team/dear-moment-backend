package kr.kro.dearmoment.like.adapter.output.persistence

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.RenderContext
import com.linecorp.kotlinjdsl.support.hibernate.extension.createQuery
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.like.domain.ProductOptionLike
import kr.kro.dearmoment.like.domain.StudioLike
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionEntity
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class LikeReadOnlyPersistenceAdapter(
    private val productOptionLikeJpaRepository: ProductOptionLikeJpaRepository,
    private val studioLikeJpaRepository: StudioLikeJpaRepository,
    private val entityManager: EntityManager,
    private val jpqlRenderContext: RenderContext,
) : GetLikePort {
    override fun findUserStudioLikes(
        userId: Long,
        pageable: Pageable,
    ): Page<StudioLike> {
        return studioLikeJpaRepository.findPage(pageable) {
            select(
                entity(StudioLikeEntity::class),
            ).from(
                entity(StudioLikeEntity::class),
                fetchJoin(StudioLikeEntity::studio),
            ).where(
                path(StudioLikeEntity::userId).eq(userId),
            )
        }.map { it?.toDomain() }
    }

    override fun findUserProductOptionLikes(
        userId: Long,
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

    override fun existStudioLike(
        userId: Long,
        studioId: Long,
    ): Boolean {
        val query =
            jpql {
                select(
                    path(StudioLikeEntity::id),
                ).from(
                    entity(StudioLikeEntity::class),
                ).where(
                    and(
                        path(StudioLikeEntity::userId).eq(userId),
                        path(StudioLikeEntity::studio).path(StudioEntity::id).eq(studioId),
                    ),
                )
            }
        val likeId = entityManager.createQuery(query, jpqlRenderContext).resultList

        return likeId.isNotEmpty()
    }

    override fun existProductOptionLike(
        userId: Long,
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
}
