package kr.kro.dearmoment.inquiry.application.command

import kr.kro.dearmoment.inquiry.domain.ServiceInquiry
import kr.kro.dearmoment.inquiry.domain.ServiceInquiryType

data class CreateServiceInquiryCommand(
    val userId: Long,
    val type: String,
    val content: String,
    val email: String,
) {
    fun toDomain() =
        ServiceInquiry(
            userId = userId,
            type = ServiceInquiryType.from(type),
            content = content,
        )
}
