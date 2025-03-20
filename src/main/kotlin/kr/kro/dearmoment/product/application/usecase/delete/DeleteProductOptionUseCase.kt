package kr.kro.dearmoment.product.application.usecase.delete

/**
 * [상품 옵션] 삭제 전용 UseCase
 */
interface DeleteProductOptionUseCase {
    /**
     * 지정된 상품(productId)의 옵션(optionId)을 삭제한다.
     * 만약 해당 옵션이 해당 상품에 속하지 않는 경우 예외를 던진다.
     */
    fun deleteOption(
        productId: Long,
        optionId: Long,
    )
}
