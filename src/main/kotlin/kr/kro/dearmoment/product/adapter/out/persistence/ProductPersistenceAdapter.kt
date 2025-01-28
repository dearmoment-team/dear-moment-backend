package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
class ProductPersistenceAdapter(
    private val jpaProductRepository: JpaProductRepository,
    private val jpaProductOptionRepository: JpaProductOptionRepository,
) : ProductPersistencePort {
    override fun save(product: Product): Product {
        val entity = ProductEntity.fromDomain(product)
        return jpaProductRepository.saveAndFlush(entity).toDomain()
    }

    override fun findById(id: Long): Product? {
        return jpaProductRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findAll(): List<Product> {
        return jpaProductRepository.findAll().map { it.toDomain() }
    }

    override fun findByUserId(userId: Long): List<Product> {
        return jpaProductRepository.findByUserId(userId).map { it.toDomain() }
    }

    override fun existsById(id: Long): Boolean {
        return jpaProductRepository.existsById(id)
    }

    override fun searchByCriteria(
        title: String?,
        priceRange: Pair<Long?, Long?>?,
        typeCode: Int?,
        sortBy: String?,
    ): List<Product> {
        val (minPrice, maxPrice) = priceRange ?: Pair(null, null)
        return jpaProductRepository.searchByCriteria(
            title = title,
            minPrice = minPrice,
            maxPrice = maxPrice,
            typeCode = typeCode,
            sortBy = sortBy,
        ).map { it.toDomain() }
    }

    @Transactional
    override fun deleteById(id: Long) {
        jpaProductOptionRepository.deleteAllByProductProductId(id)
        jpaProductRepository.deleteById(id)
    }

    /**
     * 사용자 ID와 상품명 조합으로 중복 확인
     * - 상품 생성 시 동일 사용자의 중복 상품명 검증용
     */
    override fun existsByUserIdAndTitle(
        userId: Long,
        title: String,
    ): Boolean {
        return jpaProductRepository.existsByUserIdAndTitle(userId, title)
    }
}
