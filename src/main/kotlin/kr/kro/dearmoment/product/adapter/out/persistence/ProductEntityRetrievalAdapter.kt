package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Repository

@Repository
class ProductEntityRetrievalAdapter(
    private val jpaProductRepository: JpaProductRepository
) : ProductEntityRetrievalPort {

    override fun getProductById(id: Long): Product {
        val entity = jpaProductRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Product with ID $id not found") }
        return entity.toDomain()
    }

    override fun getAllProducts(): List<Product> {
        return jpaProductRepository.findAll().map { it.toDomain() }
    }

    override fun getProductsByUserId(userId: Long): List<Product> {
        return jpaProductRepository.findByUserId(userId).map { it.toDomain() }
    }

    override fun existsById(id: Long): Boolean {
        return jpaProductRepository.existsById(id)
    }
}