package kr.kro.dearmoment.inquiry.domain

import java.util.UUID

class ServiceInquiry(
    id: Long = 0L,
    userId: UUID,
    val type: ServiceInquiryType,
    val content: String,
    val answered: Boolean = false,
) : Inquiry(id, userId) {
    init {
        require(content.isNotEmpty()) { "내용은 존재해야 합니다." }
    }
}
