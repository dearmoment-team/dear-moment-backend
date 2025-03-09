package kr.kro.dearmoment.like.adapter.output.persistence

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.like.application.port.output.DeleteLikePort
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort
import kr.kro.dearmoment.like.domain.CreateProductOptionLike
import kr.kro.dearmoment.like.domain.CreateStudioLike
import kr.kro.dearmoment.like.domain.ProductOptionLike
import kr.kro.dearmoment.like.domain.StudioLike
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductOptionRepository
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class LikePersistenceAdapter(
    private val studioLikeRepository: StudioLikeJpaRepository,
    private val productOptionLikeRepository: ProductOptionLikeJpaRepository,
    private val studioRepository: StudioJpaRepository,
    private val productOptionRepository: JpaProductOptionRepository,
) : SaveLikePort, DeleteLikePort, GetLikePort {
    override fun saveStudioLike(like: CreateStudioLike): Long {
        val studio =
            studioRepository.findByIdOrNull(like.studioId)
                ?: throw CustomException(ErrorCode.STUDIO_NOT_FOUND)
        val entity = StudioLikeEntity.from(like, studio)

        return studioLikeRepository.save(entity).id
    }

    override fun saveProductOptionLike(like: CreateProductOptionLike): Long {
        val option =
            productOptionRepository.findByIdOrNull(like.productOptionId)
                ?: throw CustomException(ErrorCode.PRODUCT_OPTION_NOT_FOUND)
        val entity = ProductOptionLikeEntity.from(like, option)

        return productOptionLikeRepository.save(entity).id
    }

    override fun findUserStudioLikes(userId: Long): List<StudioLike> = studioLikeRepository.getUserLikes(userId).map { it.toDomain() }

    override fun findUserProductLikes(userId: Long): List<ProductOptionLike> =
        productOptionLikeRepository.getUserLikes(userId).map { it.toDomain() }

    override fun existStudioLike(
        userId: Long,
        studioId: Long,
    ): Boolean = studioLikeRepository.existsByUserIdAndStudioId(userId, studioId)

    override fun existProductOptionLike(
        userId: Long,
        productOptionId: Long,
    ): Boolean = productOptionLikeRepository.existsByUserIdAndOptionOptionId(userId, productOptionId)

    override fun deleteStudioLike(likeId: Long) {
        studioLikeRepository.deleteById(likeId)
    }

    override fun deleteProductOptionLike(likeId: Long) {
        productOptionLikeRepository.deleteById(likeId)
    }
}
