package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ProductPersistenceAdapter(
    private val studioRepository: StudioJpaRepository,
    private val jpaProductRepository: JpaProductRepository,
    private val jpaProductOptionRepository: JpaProductOptionRepository,
) : ProductPersistencePort {
    override fun save(
        product: Product,
        studioId: Long,
    ): Product {
        val studio = studioRepository.findByIdOrNull(studioId) ?: throw CustomException(ErrorCode.STUDIO_NOT_FOUND)
        val entity = ProductEntity.fromDomain(product, studio)
        return jpaProductRepository.saveAndFlush(entity).toDomain()
    }

    override fun deleteById(id: Long) {
        jpaProductOptionRepository.deleteAllByProductProductId(id)
        jpaProductRepository.deleteById(id)
    }

    override fun increaseLikeCount(productId: Long) {
        jpaProductRepository.increaseLikeCount(productId)
    }

    override fun decreaseLikeCount(productId: Long) {
        jpaProductRepository.decreaseLikeCount(productId)
    }

    override fun increaseOptionLikeCount(productId: Long) {
        jpaProductRepository.increaseOptionLikeCount(productId)
    }

    override fun decreaseOptionLikeCount(productId: Long) {
        jpaProductRepository.decreaseOptionLikeCount(productId)
    }

    override fun increaseInquiryCount(productId: Long) {
        jpaProductRepository.increaseInquiryCount(productId)
    }

    override fun decreaseInquiryCount(productId: Long) {
        jpaProductRepository.decreaseInquiryCount(productId)
    }
}
