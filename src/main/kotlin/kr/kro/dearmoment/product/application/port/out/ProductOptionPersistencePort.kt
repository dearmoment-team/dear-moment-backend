package kr.kro.dearmoment.product.application.port.out

import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption

interface ProductOptionPersistencePort {
    /**
     * ProductOption을 저장합니다.
     * @param productOption 저장할 ProductOption 도메인 모델
     * @param product 연결된 ProductEntity
     * @return 저장된 ProductOption 도메인 모델
     */
    fun save(
        productOption: ProductOption,
        product: Product,
    ): ProductOption

    /**
     * ID를 기준으로 ProductOption을 삭제합니다.
     * @param id 삭제할 ProductOption의 ID
     */
    fun deleteById(id: Long)

    /**
     * 특정 Product ID에 속한 모든 ProductOption을 삭제합니다.
     * @param productId 삭제할 Product ID
     */
    fun deleteAllByProductId(productId: Long)
}
