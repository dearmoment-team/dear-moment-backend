package kr.kro.dearmoment.inquiry.domain

class ServiceInquiry(
    id: Long = 0L,
    userId: Long = 0L,
    val type: ServiceInquiryType,
    val content: String,
    val answered: Boolean = false,
) : Inquiry(id, userId) {
    init {
        require(content.isNotEmpty()) { "내용은 존재해야 합니다." }
    }
}
