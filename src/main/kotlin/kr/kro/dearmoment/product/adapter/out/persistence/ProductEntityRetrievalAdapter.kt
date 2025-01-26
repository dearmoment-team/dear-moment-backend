package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import org.springframework.stereotype.Repository

@Repository
class ProductEntityRetrievalAdapter(
    private val jpaProductRepository: JpaProductRepository
) : ProductEntityRetrievalPort {
    override fun getEntityById(id: Long): ProductEntity {
        return jpaProductRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product with ID $id not found") }
    }
}