package kr.kro.dearmoment.inquiry.domain

class CreateProductOptionInquiry(
    id: Long = 0L,
    userId: Long = 0L,
    val productOptionId: Long,
) : Inquiry(id, userId) {
    init {
        require(productOptionId > 0) { "상품 옵션 ID는 양수여야 합니다." }
    }
}
