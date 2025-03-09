package kr.kro.dearmoment.inquiry.application.port.input

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.inquiry.application.dto.GetProductOptionInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.GetStudioInquiryResponse
import kr.kro.dearmoment.inquiry.application.query.GetProductInquiresQuery
import kr.kro.dearmoment.inquiry.application.query.GetStudioInquiresQuery

interface GetInquiryUseCase {
    fun getStudioInquiries(query: GetStudioInquiresQuery): PagedResponse<GetStudioInquiryResponse>

    fun getProductOptionInquiries(query: GetProductInquiresQuery): PagedResponse<GetProductOptionInquiryResponse>
}
