package kr.kro.dearmoment.product.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaProductOptionRepository : JpaRepository<ProductOptionEntity, Long> {
    /**
     * 특정 ProductEntity와 옵션 이름으로 ProductOption의 존재 여부를 확인합니다.
     */
    fun existsByProductProductIdAndName(
        productId: Long,
        name: String,
    ): Boolean

    /**
     * 특정 Product ID에 속한 모든 ProductOption을 조회합니다.
     */
    fun findByProductProductId(productId: Long): List<ProductOptionEntity>

    /**
     * 특정 Product ID에 속한 모든 ProductOption을 삭제합니다.
     */
    fun deleteAllByProductProductId(productId: Long)

    /**
     * 특정 Product ID에 속한 ProductOption의 존재 여부를 확인합니다.
     */
    fun existsByProductProductId(productId: Long): Boolean

}
