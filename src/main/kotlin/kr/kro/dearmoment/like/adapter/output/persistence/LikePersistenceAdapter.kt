package kr.kro.dearmoment.like.adapter.output.persistence

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.like.application.port.output.DeleteLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort
import kr.kro.dearmoment.like.domain.CreateProductLike
import kr.kro.dearmoment.like.domain.CreateProductOptionLike
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductOptionRepository
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class LikePersistenceAdapter(
    private val productLikeRepository: ProductLikeJpaRepository,
    private val productOptionLikeRepository: ProductOptionLikeJpaRepository,
    private val productRepository: JpaProductRepository,
    private val productOptionRepository: JpaProductOptionRepository,
) : SaveLikePort, DeleteLikePort {
    override fun saveProductLike(like: CreateProductLike): Long {
        val product =
            productRepository.findByIdOrNull(like.productId)
                ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)
        val entity = ProductLikeEntity.from(like, product)

        return try {
            productLikeRepository.save(entity).id
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

    override fun deleteProductLike(
        userId: UUID,
        likeId: Long,
    ) {
        val entity =
            productLikeRepository.findByIdAndUserId(likeId, userId)
                ?: throw CustomException(ErrorCode.LIKE_NOT_FOUND)
        productLikeRepository.delete(entity)
    }

    override fun deleteProductOptionLike(
        userId: UUID,
        likeId: Long,
    ) {
        val entity =
            productOptionLikeRepository.findByIdAndUserId(likeId, userId)
                ?: throw CustomException(ErrorCode.LIKE_NOT_FOUND)
        productOptionLikeRepository.delete(entity)
    }
}
