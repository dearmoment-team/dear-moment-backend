package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ProductOptionRepositoryAdapter(
    private val jpaProductOptionRepository: JpaProductOptionRepository,
    private val jpaProductRepository: JpaProductRepository,
    private val productEntityRetrievalPort: ProductEntityRetrievalPort,
) : ProductOptionPersistencePort {

    /**
     * ProductOption을 저장합니다.
     * - productId가 null인 경우 예외를 던집니다.
     * - 동일한 이름의 옵션이 이미 존재하는지 확인합니다.
     * - 새로운 ProductOptionEntity를 생성하여 저장합니다.
     */
    @Transactional
    override fun save(productOption: ProductOption): ProductOption {
        val productId = productOption.productId
            ?: throw IllegalArgumentException("Product ID must be provided")

        // 영속성 컨텍스트에서 ProductEntity를 조회
        val productEntity = jpaProductRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product with ID $productId not found") }

        // 동일한 이름의 옵션이 이미 존재하는지 확인 (효율적인 방법)
        if (jpaProductOptionRepository.existsByProductAndName(productEntity, productOption.name)) {
            throw IllegalArgumentException("ProductOption already exists: ${productOption.name}")
        }

        // 새로운 ProductOptionEntity 생성
        val optionEntity = ProductOptionEntity.fromDomain(productOption, productEntity)

        // 옵션 저장
        val savedEntity = jpaProductOptionRepository.save(optionEntity)

        return savedEntity.toDomain()
    }

    /**
     * ID로 ProductOption을 조회합니다.
     * - 존재하지 않는 ID인 경우 예외를 던집니다.
     */
    @Transactional(readOnly = true)
    override fun findById(id: Long): ProductOption {
        return jpaProductOptionRepository.findById(id)
            .orElseThrow { IllegalArgumentException("ProductOption with ID $id not found") }
            .toDomain()
    }

    /**
     * 모든 ProductOption을 조회합니다.
     */
    @Transactional(readOnly = true)
    override fun findAll(): List<ProductOption> {
        return jpaProductOptionRepository.findAll().map { it.toDomain() }
    }

    /**
     * ID로 ProductOption을 삭제합니다.
     * - 존재하지 않는 ID인 경우 예외를 던집니다.
     */
    @Transactional
    override fun deleteById(id: Long) {
        if (!jpaProductOptionRepository.existsById(id)) {
            throw IllegalArgumentException("Cannot delete ProductOption: ID $id not found.")
        }
        jpaProductOptionRepository.deleteById(id)
    }

    /**
     * 특정 Product에 속한 모든 ProductOption을 조회합니다.
     */
    @Transactional(readOnly = true)
    override fun findByProduct(product: Product): List<ProductOption> {
        val productId = product.productId
            ?: throw IllegalArgumentException("Product ID must be provided for finding options")

        val productEntity = jpaProductRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product with ID $productId not found") }

        return jpaProductOptionRepository.findByProduct(productEntity).map { it.toDomain() }
    }

    /**
     * 특정 Product ID에 속한 모든 ProductOption을 삭제합니다.
     */
    @Transactional
    override fun deleteAllByProductId(productId: Long) {
        jpaProductOptionRepository.deleteAllByProductProductId(productId)
    }

    /**
     * 특정 Product ID에 속한 ProductOption의 존재 여부를 확인합니다.
     */
    @Transactional(readOnly = true)
    fun existsByProductId(productId: Long): Boolean {
        return jpaProductOptionRepository.existsByProductProductId(productId)
    }
}