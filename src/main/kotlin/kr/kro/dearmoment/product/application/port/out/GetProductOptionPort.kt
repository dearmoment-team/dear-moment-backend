package kr.kro.dearmoment.product.application.port.out

import kr.kro.dearmoment.product.domain.model.option.ProductOption

interface GetProductOptionPort {
    /**
     * 특정 Product ID에 속한 ProductOption의 존재 여부를 확인합니다.
     * @param productId 확인할 Product ID
     * @return ProductOption 존재 여부
     */
    fun existsByProductId(productId: Long): Boolean

    /**
     * 특정 Product ID와 옵션 이름으로 중복 여부 확인
     * @param productId Product ID
     * @param name 옵션 이름
     * @return 중복 여부
     */
    fun existsByProductIdAndName(
        productId: Long,
        name: String,
    ): Boolean

    /**
     * 특정 Product에 속한 모든 ProductOption을 조회합니다.
     * @param productId 조회할 Product ID
     * @return 해당 Product에 속한 ProductOption 도메인 모델 리스트
     */
    fun findByProductId(productId: Long): List<ProductOption>

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
}
