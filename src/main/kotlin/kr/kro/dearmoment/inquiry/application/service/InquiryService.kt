package kr.kro.dearmoment.inquiry.application.service

import kr.kro.dearmoment.inquiry.adapter.input.web.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.command.CreateAuthorInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateProductInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateServiceInquiryCommand
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.RemoveInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.output.DeleteInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.SaveInquiryPort
import org.springframework.stereotype.Service

@Service
class InquiryService(
    private val saveInquiryPort: SaveInquiryPort,
    private val deleteInquiryPort: DeleteInquiryPort,
) : CreateInquiryUseCase, RemoveInquiryUseCase {
    override fun createAuthorInquiry(command: CreateAuthorInquiryCommand): CreateInquiryResponse {
        val inquiry = CreateAuthorInquiryCommand.toDomain(command)
        return CreateInquiryResponse(saveInquiryPort.saveAuthorInquiry(inquiry))
    }

    override fun createProductInquiry(command: CreateProductInquiryCommand): CreateInquiryResponse {
        val inquiry = CreateProductInquiryCommand.toDomain(command)
        return CreateInquiryResponse(saveInquiryPort.saveProductInquiry(inquiry))
    }

    override fun createServiceInquiry(command: CreateServiceInquiryCommand): CreateInquiryResponse {
        val inquiry = CreateServiceInquiryCommand.toDomain(command)
        return CreateInquiryResponse(saveInquiryPort.saveServiceInquiry(inquiry))
    }

    override fun removeProductInquiry(inquiryId: Long): Unit = deleteInquiryPort.deleteProductInquiry(inquiryId)
}
