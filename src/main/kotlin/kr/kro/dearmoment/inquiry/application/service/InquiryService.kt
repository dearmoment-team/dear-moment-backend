package kr.kro.dearmoment.inquiry.application.service

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.inquiry.application.command.CreateArtistInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateProductInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateServiceInquiryCommand
import kr.kro.dearmoment.inquiry.application.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.GetArtistInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.GetProductInquiryResponse
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.GetInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.RemoveInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.output.DeleteInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.GetInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.SaveInquiryPort
import kr.kro.dearmoment.inquiry.application.port.output.SendInquiryPort
import kr.kro.dearmoment.inquiry.application.query.GetArtistInquiresQuery
import kr.kro.dearmoment.inquiry.application.query.GetProductInquiresQuery
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
    override fun createArtistInquiry(command: CreateArtistInquiryCommand): CreateInquiryResponse {
        val inquiry = command.toDomain()
        val savedInquiryId = saveInquiryPort.saveArtistInquiry(inquiry)

        sendInquiryPort.sendMail(
            email = command.email,
            subject = "[작가 정보 오류 제보] ${inquiry.title}",
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
            subject = "[고객의 소리] ${inquiry.type.desc}",
            body = inquiry.content,
        )
        return CreateInquiryResponse(savedInquiryId)
    }

    @Transactional(readOnly = true)
    override fun getArtistInquiries(query: GetArtistInquiresQuery): PagedResponse<GetArtistInquiryResponse> {
        val inquiries = getInquiryPort.getArtistInquiries(query.userId, query.pageable)

        return PagedResponse(
            content = inquiries.content.map { GetArtistInquiryResponse.from(it) },
            page = query.pageable.pageNumber,
            size = query.pageable.pageSize,
            totalElements = inquiries.totalElements,
            totalPages = inquiries.totalPages,
        )
    }

    @Transactional(readOnly = true)
    override fun getProductInquiries(query: GetProductInquiresQuery): PagedResponse<GetProductInquiryResponse> {
        val inquiries = getInquiryPort.getProductInquiries(query.userId, query.pageable)
        return PagedResponse(
            content = inquiries.content.map { GetProductInquiryResponse.from(it) },
            page = query.pageable.pageNumber,
            size = query.pageable.pageSize,
            totalElements = inquiries.totalElements,
            totalPages = inquiries.totalPages,
        )
    }

    @Transactional
    override fun removeProductInquiry(inquiryId: Long): Unit = deleteInquiryPort.deleteProductInquiry(inquiryId)
}
