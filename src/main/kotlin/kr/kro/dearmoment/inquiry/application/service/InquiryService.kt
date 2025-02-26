package kr.kro.dearmoment.inquiry.application.service

import kr.kro.dearmoment.inquiry.adapter.input.web.author.dto.GetAuthorInquiriesResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.author.dto.GetAuthorInquiryResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.product.dto.GetProductInquiriesResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.product.dto.GetProductInquiryResponse
import kr.kro.dearmoment.inquiry.application.command.CreateAuthorInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateProductInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateServiceInquiryCommand
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.GetInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.RemoveInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.output.DeleteInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.GetInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.SaveInquiryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InquiryService(
    private val saveInquiryPort: SaveInquiryPort,
    private val getInquiryPort: GetInquiryPort,
    private val deleteInquiryPort: DeleteInquiryPort,
) : CreateInquiryUseCase, GetInquiryUseCase, RemoveInquiryUseCase {
    @Transactional
    override fun createAuthorInquiry(command: CreateAuthorInquiryCommand): CreateInquiryResponse {
        val inquiry = command.toDomain()
        return CreateInquiryResponse(saveInquiryPort.saveAuthorInquiry(inquiry))
    }

    @Transactional
    override fun createProductInquiry(command: CreateProductInquiryCommand): CreateInquiryResponse {
        val inquiry = command.toDomain()
        return CreateInquiryResponse(saveInquiryPort.saveProductInquiry(inquiry))
    }

    @Transactional
    override fun createServiceInquiry(command: CreateServiceInquiryCommand): CreateInquiryResponse {
        val inquiry = command.toDomain()
        return CreateInquiryResponse(saveInquiryPort.saveServiceInquiry(inquiry))
    }

    @Transactional(readOnly = true)
    override fun getAuthorInquiries(userId: Long): GetAuthorInquiriesResponse {
        val inquiries = getInquiryPort.getAuthorInquiries(userId)
        return GetAuthorInquiriesResponse(inquiries.map { GetAuthorInquiryResponse.from(it) })
    }

    @Transactional(readOnly = true)
    override fun getProductInquiries(userId: Long): GetProductInquiriesResponse {
        val inquiries = getInquiryPort.getProductInquiries(userId)
        return GetProductInquiriesResponse(inquiries.map { GetProductInquiryResponse.from(it) })
    }

    @Transactional
    override fun removeProductInquiry(inquiryId: Long): Unit = deleteInquiryPort.deleteProductInquiry(inquiryId)
}
