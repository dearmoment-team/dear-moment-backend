package kr.kro.dearmoment.product.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface JpaProductOptionRepository : JpaRepository<ProductOptionEntity, Long> {

    /**
     * 특정 ProductEntity에 속하는 ProductOptionEntity 리스트를 반환합니다.
     * @param product 대상 ProductEntity
     * @return 연결된 ProductOptionEntity 리스트
     */
    fun findByProduct(product: ProductEntity): List<ProductOptionEntity>
}
