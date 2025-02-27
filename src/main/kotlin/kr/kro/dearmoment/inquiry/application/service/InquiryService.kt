package kr.kro.dearmoment.inquiry.application.service

import kr.kro.dearmoment.common.dto.PagedResponse
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
import kr.kro.dearmoment.inquiry.application.port.output.SendInquiryPort
import kr.kro.dearmoment.inquiry.application.query.GetAuthorInquiresQuery
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InquiryService(
    private val saveInquiryPort: SaveInquiryPort,
    private val getInquiryPort: GetInquiryPort,
    private val deleteInquiryPort: DeleteInquiryPort,
    private val sendInquiryPort: SendInquiryPort,
) : CreateInquiryUseCase, GetInquiryUseCase, RemoveInquiryUseCase {
    @Transactional
    override fun createAuthorInquiry(command: CreateAuthorInquiryCommand): CreateInquiryResponse {
        val inquiry = command.toDomain()
        val savedInquiryId = saveInquiryPort.saveAuthorInquiry(inquiry)

        sendInquiryPort.sendMail(
            email = command.email,
            subject = inquiry.title,
            body = inquiry.content,
        )

        return CreateInquiryResponse(savedInquiryId)
    }

    @Transactional
    override fun createProductInquiry(command: CreateProductInquiryCommand): CreateInquiryResponse {
        val inquiry = command.toDomain()
        return CreateInquiryResponse(saveInquiryPort.saveProductInquiry(inquiry))
    }

    @Transactional
    override fun createServiceInquiry(command: CreateServiceInquiryCommand): CreateInquiryResponse {
        val inquiry = command.toDomain()
        val savedInquiryId = saveInquiryPort.saveServiceInquiry(inquiry)

        sendInquiryPort.sendMail(
            email = command.email,
            subject = inquiry.type.desc,
            body = inquiry.content,
        )
        return CreateInquiryResponse(savedInquiryId)
    }

    @Transactional(readOnly = true)
    override fun getAuthorInquiries(query: GetAuthorInquiresQuery): PagedResponse<GetAuthorInquiryResponse> {
        val inquiries = getInquiryPort.getAuthorInquiries(query.userId, query.pageable)
        return PagedResponse(
            content = inquiries.content.map { GetAuthorInquiryResponse.from(it) },
            page = query.pageable.pageNumber,
            size = query.pageable.pageSize,
            totalElements = inquiries.totalElements,
            totalPages = inquiries.totalPages,
        )
    }

    @Transactional(readOnly = true)
    override fun getProductInquiries(userId: Long): GetProductInquiriesResponse {
        val inquiries = getInquiryPort.getProductInquiries(userId)
        return GetProductInquiriesResponse(inquiries.map { GetProductInquiryResponse.from(it) })
    }

    @Transactional
    override fun removeProductInquiry(inquiryId: Long): Unit = deleteInquiryPort.deleteProductInquiry(inquiryId)
}
