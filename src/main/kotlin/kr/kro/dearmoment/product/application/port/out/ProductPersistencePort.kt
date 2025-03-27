package kr.kro.dearmoment.product.application.port.out

import kr.kro.dearmoment.product.domain.model.Product

interface ProductPersistencePort {
    /**
     * Product를 저장합니다.
     * @param product 저장할 도메인 모델
     * @return 저장된 Product
     */
    fun save(
        product: Product,
        studioId: Long,
    ): Product

    /**
     * ID를 기준으로 Product를 삭제합니다.
     * @param id 삭제할 Product의 ID
     */
    fun deleteById(id: Long)

    fun increaseLikeCount(productId: Long)

    fun decreaseLikeCount(productId: Long)

    fun increaseOptionLikeCount(productId: Long)

    fun decreaseOptionLikeCount(productId: Long)

    fun increaseInquiryCount(productId: Long)

    fun decreaseInquiryCount(productId: Long)
}
