package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
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

    /**
     * 검색 조건은 제목, 상품유형, 촬영장소, 정렬 조건을 기반으로 함.
     * 전달받은 productType과 shootingPlace는 문자열(String) 형태로 받고,
     * 어댑터 내부에서 해당 enum(ProductType, ShootingPlace)으로 변환하여 쿼리에 전달합니다.
     */
    override fun searchByCriteria(
        title: String?,
        productType: String?,
        shootingPlace: String?,
        sortBy: String?,
    ): List<Product> {
        val pt: ProductType? = productType?.let { ProductType.valueOf(it) }
        val sp: ShootingPlace? = shootingPlace?.let { ShootingPlace.valueOf(it) }
        val productEntities = jpaProductRepository.searchByCriteria(title, pt, sp, sortBy)
        return productEntities.map { it.toDomain() }
    }

    @Transactional
    override fun deleteById(id: Long) {
        jpaProductOptionRepository.deleteAllByProductProductId(id)
        jpaProductRepository.deleteById(id)
    }

    override fun existsByUserIdAndTitle(
        userId: Long,
        title: String,
    ): Boolean {
        return jpaProductRepository.existsByUserIdAndTitle(userId, title)
    }
}
