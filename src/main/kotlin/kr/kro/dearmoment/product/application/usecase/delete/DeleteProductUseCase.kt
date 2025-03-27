package kr.kro.dearmoment.product.application.usecase.delete

import java.util.UUID

interface DeleteProductUseCase {
    /**
     * 인증된 사용자(userId)가 소유한 상품(productId)만 삭제할 수 있도록 한다.
     */
    fun deleteProduct(
        userId: UUID,
        productId: Long,
    )
}
