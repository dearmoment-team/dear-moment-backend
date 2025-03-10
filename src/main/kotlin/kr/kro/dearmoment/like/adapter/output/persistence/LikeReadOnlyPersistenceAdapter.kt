package kr.kro.dearmoment.like.adapter.output.persistence

import kr.kro.dearmoment.like.domain.ProductOptionLike
import kr.kro.dearmoment.like.domain.StudioLike
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class LikeReadOnlyPersistenceAdapter(
    private val productOptionLikeJpaRepository: ProductOptionLikeJpaRepository,
    private val studioLikeJpaRepository: StudioLikeJpaRepository,
) {
    fun findUserStudioLikes(
        userId: Long,
        pageable: Pageable,
    ): Page<StudioLike> {
        return studioLikeJpaRepository.findPage(pageable) {
            select(
                entity(StudioLikeEntity::class),
            ).from(
                entity(StudioLikeEntity::class),
                fetchJoin(StudioLikeEntity::studio),
            )
        }.map { it?.toDomain() }
    }

    fun findUserProductOptionLikes(
        userId: Long,
        pageable: Pageable,
    ): Page<ProductOptionLike> {
        return productOptionLikeJpaRepository.findPage(pageable) {
            select(
                entity(ProductOptionLikeEntity::class),
            ).from(
                entity(ProductOptionLikeEntity::class),
                fetchJoin(ProductOptionLikeEntity::option),
            )
        }.map { it?.toDomain() }
    }

    fun existStudioLike(
        userId: Long,
        studioId: Long,
    ): Boolean {
        TODO("Not yet implemented")
    }

    fun existProductOptionLike(
        userId: Long,
        productOptionId: Long,
    ): Boolean {
        TODO("Not yet implemented")
    }
}
