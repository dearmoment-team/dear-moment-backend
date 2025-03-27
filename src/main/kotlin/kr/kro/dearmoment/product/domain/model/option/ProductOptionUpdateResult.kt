package kr.kro.dearmoment.product.domain.model.option

/**
 * 옵션 업데이트 시 결과를 담는 DTO
 */
data class ProductOptionUpdateResult(
    val updatedOptions: List<ProductOption>,
    val deletedOptionIds: Set<Long>,
)
