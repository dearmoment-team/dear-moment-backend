package kr.kro.dearmoment.product.application.port.out

import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.domain.model.ProductOption

interface ProductOptionPersistencePort {
    /**
     * ProductOption을 저장합니다.
     * @param productOption 저장할 ProductOption 도메인 모델
     * @param productEntity 연결된 ProductEntity
     * @return 저장된 ProductOption 도메인 모델
     */
    fun save(
        productOption: ProductOption,
        productEntity: ProductEntity,
    ): ProductOption

    /**
     * ID를 기준으로 ProductOption을 조회합니다.
     * @param id 조회할 ProductOption의 ID
     * @return 조회된 ProductOption 도메인 모델
     */
    fun findById(id: Long): ProductOption

    /**
     * 모든 ProductOption을 조회합니다.
     * @return 조회된 ProductOption 도메인 모델 리스트
     */
    fun findAll(): List<ProductOption>

    /**
     * ID를 기준으로 ProductOption을 삭제합니다.
     * @param id 삭제할 ProductOption의 ID
     */
    fun deleteById(id: Long)

    /**
     * 특정 Product에 속한 모든 ProductOption을 조회합니다.
     * @param productId 조회할 Product ID
     * @return 해당 Product에 속한 ProductOption 도메인 모델 리스트
     */
    fun findByProductId(productId: Long): List<ProductOption>

    /**
     * 특정 Product ID에 속한 모든 ProductOption을 삭제합니다.
     * @param productId 삭제할 Product ID
     */
    fun deleteAllByProductId(productId: Long)

    /**
     * 특정 Product ID에 속한 ProductOption의 존재 여부를 확인합니다.
     * @param productId 확인할 Product ID
     * @return ProductOption 존재 여부
     */
    fun existsByProductId(productId: Long): Boolean
}
