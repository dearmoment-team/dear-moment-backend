package kr.kro.dearmoment.inquiry.application.command

import kr.kro.dearmoment.inquiry.domain.ServiceInquiry
import kr.kro.dearmoment.inquiry.domain.ServiceInquiryType
import java.util.UUID

data class CreateServiceInquiryCommand(
    val userId: UUID,
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
