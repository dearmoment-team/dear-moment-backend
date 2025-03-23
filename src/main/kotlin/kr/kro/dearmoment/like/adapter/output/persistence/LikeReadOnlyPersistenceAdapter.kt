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

@Repository
class LikeReadOnlyPersistenceAdapter(
    private val productOptionLikeJpaRepository: ProductOptionLikeJpaRepository,
    private val productLikeJpaRepository: ProductLikeJpaRepository,
    private val entityManager: EntityManager,
    private val jpqlRenderContext: RenderContext,
) : GetLikePort {
    override fun findUserProductLikes(
        userId: Long,
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

    override fun existProductLike(
        userId: Long,
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
