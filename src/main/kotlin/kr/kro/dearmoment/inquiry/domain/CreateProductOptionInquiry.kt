package kr.kro.dearmoment.inquiry.domain

class CreateProductOptionInquiry(
    id: Long = 0L,
    userId: Long = 0L,
    val productId: Long,
    val optionId: Long,
) : Inquiry(id, userId) {
    init {
        require(productId > 0) { "상품 옵션 ID는 양수여야 합니다." }
        require(optionId > 0) { "상품 옵션 ID는 양수여야 합니다." }
    }
}
