package kr.kro.dearmoment.product.adapter.out.persistence

import com.linecorp.kotlinjdsl.render.RenderContext
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.product.application.port.out.GetProductOptionPort
import kr.kro.dearmoment.product.domain.model.option.ProductOption
import org.springframework.stereotype.Repository

@Repository
class ProductOptionReadOnlyRepository(
    private val productOptionRepository: JpaProductOptionRepository,
    private val entityManager: EntityManager,
    private val jpqlRenderContext: RenderContext,
) : GetProductOptionPort {
    override fun findById(id: Long): ProductOption {
        return productOptionRepository.findById(id)
            .orElseThrow { CustomException(ErrorCode.OPTION_NOT_FOUND) }
            .toDomain()
    }

    override fun findAll(): List<ProductOption> {
        return productOptionRepository.findAll().map { it.toDomain() }
    }

    override fun findByProductId(productId: Long): List<ProductOption> {
        return productOptionRepository.findByProductProductId(productId).map { it.toDomain() }
    }

    override fun existsByProductId(productId: Long): Boolean {
        return productOptionRepository.existsByProductProductId(productId)
    }

    override fun existsByProductIdAndName(
        productId: Long,
        name: String,
    ): Boolean {
        return productOptionRepository.existsByProductProductIdAndName(productId, name)
    }
}
