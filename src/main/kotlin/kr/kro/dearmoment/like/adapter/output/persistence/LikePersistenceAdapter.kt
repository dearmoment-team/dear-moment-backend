package kr.kro.dearmoment.like.adapter.output.persistence

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.like.application.port.output.DeleteLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort
import kr.kro.dearmoment.like.domain.CreateProductOptionLike
import kr.kro.dearmoment.like.domain.CreateStudioLike
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductOptionRepository
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class LikePersistenceAdapter(
    private val studioLikeRepository: StudioLikeJpaRepository,
    private val productOptionLikeRepository: ProductOptionLikeJpaRepository,
    private val studioRepository: StudioJpaRepository,
    private val productOptionRepository: JpaProductOptionRepository,
) : SaveLikePort, DeleteLikePort {
    override fun saveStudioLike(like: CreateStudioLike): Long {
        val studio =
            studioRepository.findByIdOrNull(like.studioId)
                ?: throw CustomException(ErrorCode.STUDIO_NOT_FOUND)
        val entity = StudioLikeEntity.from(like, studio)

        return try {
            studioLikeRepository.save(entity).id
        } catch (e: DataIntegrityViolationException) {
            throw CustomException(ErrorCode.LIKE_DUPLICATED)
        }
    }

    override fun saveProductOptionLike(like: CreateProductOptionLike): Long {
        val option =
            productOptionRepository.findByIdOrNull(like.productOptionId)
                ?: throw CustomException(ErrorCode.PRODUCT_OPTION_NOT_FOUND)
        val entity = ProductOptionLikeEntity.from(like, option)

        return try {
            productOptionLikeRepository.save(entity).id
        } catch (e: DataIntegrityViolationException) {
            throw CustomException(ErrorCode.LIKE_DUPLICATED)
        }
    }

    override fun deleteStudioLike(likeId: Long) {
        studioLikeRepository.deleteById(likeId)
    }

    override fun deleteProductOptionLike(likeId: Long) {
        productOptionLikeRepository.deleteById(likeId)
    }
}
