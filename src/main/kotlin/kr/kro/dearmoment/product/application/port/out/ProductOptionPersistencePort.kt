package kr.kro.dearmoment.product.application.port.out

import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption

interface ProductOptionPersistencePort {

    /**
     * ProductOption을 저장합니다.
     * @param productOption 저장할 도메인 모델
     * @return 저장된 ProductOption
     */
    fun save(productOption: ProductOption): ProductOption

    /**
     * ID를 기준으로 ProductOption을 조회합니다.
     * @param id 조회할 ProductOption의 ID
     * @return 조회된 ProductOption
     */
    fun findById(id: Long): ProductOption

    /**
     * 모든 ProductOption을 조회합니다.
     * @return 조회된 ProductOption 리스트
     */
    fun findAll(): List<ProductOption>

    /**
     * ID를 기준으로 ProductOption을 삭제합니다.
     * @param id 삭제할 ProductOption의 ID
     */
    fun deleteById(id: Long)

    /**
     * 특정 Product에 연결된 ProductOption 리스트를 조회합니다.
     * @param product 조회할 Product 도메인 모델
     * @return 조회된 ProductOption 리스트
     */
    fun findByProduct(product: Product): List<ProductOption>
}
