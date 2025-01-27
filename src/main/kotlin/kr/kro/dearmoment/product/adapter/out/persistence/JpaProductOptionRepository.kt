package kr.kro.dearmoment.product.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface JpaProductOptionRepository : JpaRepository<ProductOptionEntity, Long> {

    /**
     * 특정 ProductEntity에 속하는 ProductOptionEntity 리스트를 반환합니다.
     * @param product 대상 ProductEntity
     * @return 연결된 ProductOptionEntity 리스트
     */
    fun findByProduct(product: ProductEntity): List<ProductOptionEntity>

    /**
     * productId로 ProductOptionEntity를 찾습니다.
     */
    fun findByProductProductId(productId: Long): List<ProductOptionEntity>

    /**
     * 특정 productId를 가진 ProductOptionEntity를 모두 삭제합니다.
     * 삭제된 엔티티 수를 반환합니다.
     */
    fun deleteAllByProductProductId(productId: Long): Long
}
