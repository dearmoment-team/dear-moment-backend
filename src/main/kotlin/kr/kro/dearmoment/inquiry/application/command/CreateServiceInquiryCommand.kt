package kr.kro.dearmoment.inquiry.application.command

import kr.kro.dearmoment.inquiry.domain.ServiceInquiry
import kr.kro.dearmoment.inquiry.domain.ServiceInquiryType

data class CreateServiceInquiryCommand(
    val userId: Long,
    val type: String,
    val content: String,
) {
    companion object {
        fun toDomain(command: CreateServiceInquiryCommand) =
            ServiceInquiry(
                userId = command.userId,
                type = ServiceInquiryType.from(command.type),
                content = command.content,
            )
    }
}
