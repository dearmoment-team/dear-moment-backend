package kr.kro.dearmoment.inquiry.application.service

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
    override fun createAuthorInquiry(command: CreateAuthorInquiryCommand): Long {
        val inquiry = CreateAuthorInquiryCommand.toDomain(command)
        return saveInquiryPort.saveAuthorInquiry(inquiry)
    }

    override fun createProductInquiry(command: CreateProductInquiryCommand): Long {
        val inquiry = CreateProductInquiryCommand.toDomain(command)
        return saveInquiryPort.saveProductInquiry(inquiry)
    }

    override fun createServiceInquiry(command: CreateServiceInquiryCommand): Long {
        val inquiry = CreateServiceInquiryCommand.toDomain(command)
        return saveInquiryPort.saveServiceInquiry(inquiry)
    }

    override fun removeProductInquiry(inquiryId: Long): Unit = deleteInquiryPort.deleteProductInquiry(inquiryId)

    override fun removeAuthorInquiry(inquiryId: Long): Unit = deleteInquiryPort.deleteAuthorInquiry(inquiryId)

    override fun removeServiceInquiry(inquiryId: Long): Unit = deleteInquiryPort.deleteServiceInquiry(inquiryId)
}
