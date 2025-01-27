package kr.kro.dearmoment.product.application.port.out

import kr.kro.dearmoment.product.domain.model.Product

interface ProductEntityRetrievalPort {

    /**
     * ID를 기준으로 Product 도메인 모델을 반환합니다.
     * @param id 조회할 Product의 ID
     * @return 조회된 Product 도메인 모델
     */
    fun getProductById(id: Long?): Product

    /**
     * 모든 Product 도메인 모델을 반환합니다.
     * @return 조회된 Product 도메인 모델 리스트
     */
    fun getAllProducts(): List<Product>

    /**
     * 특정 사용자 ID에 속하는 Product 도메인 모델을 반환합니다.
     * @param userId 사용자 ID
     * @return 해당 사용자의 Product 도메인 모델 리스트
     */
    fun getProductsByUserId(userId: Long): List<Product>

    /**
     * Product가 존재하는지 확인합니다.
     * @param id Product의 ID
     * @return 존재 여부
     */
    fun existsById(id: Long): Boolean
}
