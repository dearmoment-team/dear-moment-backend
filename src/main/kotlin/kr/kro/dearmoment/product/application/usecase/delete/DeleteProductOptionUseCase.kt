package kr.kro.dearmoment.product.application.usecase.delete

import java.util.UUID

/**
 * [상품 옵션] 삭제 전용 UseCase
 */
interface DeleteProductOptionUseCase {
    /**
     * 지정된 상품(productId)의 옵션(optionId)을 삭제한다.
     * 이때, 인증된 사용자(userId)가 해당 상품의 소유자인 경우에만 삭제를 허용한다.
     */
    fun deleteOption(
        userId: UUID,
        productId: Long,
        optionId: Long,
    )
}
