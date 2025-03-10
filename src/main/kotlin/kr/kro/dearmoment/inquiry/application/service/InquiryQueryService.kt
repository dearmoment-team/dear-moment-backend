package kr.kro.dearmoment.inquiry.application.service

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.inquiry.application.dto.GetProductOptionInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.GetStudioInquiryResponse
import kr.kro.dearmoment.inquiry.application.port.input.GetInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.output.GetInquiryPort
import kr.kro.dearmoment.inquiry.application.query.GetProductInquiresQuery
import kr.kro.dearmoment.inquiry.application.query.GetStudioInquiresQuery
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class InquiryQueryService(
    private val getInquiryPort: GetInquiryPort,
) : GetInquiryUseCase {
    @Transactional(readOnly = true)
    override fun getStudioInquiries(query: GetStudioInquiresQuery): PagedResponse<GetStudioInquiryResponse> {
        val inquiries = getInquiryPort.findUserStudioInquiries(query.userId, query.pageable)

        return PagedResponse(
            content = inquiries.content.map { GetStudioInquiryResponse.from(it) },
            page = query.pageable.pageNumber,
            size = query.pageable.pageSize,
            totalElements = inquiries.totalElements,
            totalPages = inquiries.totalPages,
        )
    }

    @Transactional(readOnly = true)
    override fun getProductOptionInquiries(query: GetProductInquiresQuery): PagedResponse<GetProductOptionInquiryResponse> {
        val inquiries = getInquiryPort.findUserProductOptionInquiries(query.userId, query.pageable)
        return PagedResponse(
            content = inquiries.content.map { GetProductOptionInquiryResponse.from(it) },
            page = query.pageable.pageNumber,
            size = query.pageable.pageSize,
            totalElements = inquiries.totalElements,
            totalPages = inquiries.totalPages,
        )
    }
}
