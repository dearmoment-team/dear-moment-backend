package kr.kro.dearmoment.inquiry.application.service

import kr.kro.dearmoment.inquiry.application.command.CreateProductInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateServiceInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateStudioInquiryCommand
import kr.kro.dearmoment.inquiry.application.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.RemoveInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.output.DeleteInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.SaveInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.SendInquiryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class InquiryCommandService(
    private val saveInquiryPort: SaveInquiryPort,
    private val deleteInquiryPort: DeleteInquiryPort,
    private val sendInquiryPort: SendInquiryPort,
) : CreateInquiryUseCase, RemoveInquiryUseCase {
    override fun createStudioInquiry(command: CreateStudioInquiryCommand): CreateInquiryResponse {
        val inquiry = command.toDomain()
        val savedInquiryId = saveInquiryPort.saveStudioInquiry(inquiry)

        sendInquiryPort.sendMail(
            email = command.email,
            subject = "[작가 정보 오류 제보] ${inquiry.title}",
            body = inquiry.content,
        )

        return CreateInquiryResponse(savedInquiryId)
    }

    override fun createProductInquiry(command: CreateProductInquiryCommand): CreateInquiryResponse {
        val inquiry = command.toDomain()
        return CreateInquiryResponse(saveInquiryPort.saveProductOptionInquiry(inquiry))
    }

    override fun createServiceInquiry(command: CreateServiceInquiryCommand): CreateInquiryResponse {
        val inquiry = command.toDomain()
        val savedInquiryId = saveInquiryPort.saveServiceInquiry(inquiry)

        sendInquiryPort.sendMail(
            email = command.email,
            subject = "[고객의 소리] ${inquiry.type.desc}",
            body = inquiry.content,
        )
        return CreateInquiryResponse(savedInquiryId)
    }

    override fun removeProductInquiry(inquiryId: Long): Unit = deleteInquiryPort.deleteProductOptionInquiry(inquiryId)
}
