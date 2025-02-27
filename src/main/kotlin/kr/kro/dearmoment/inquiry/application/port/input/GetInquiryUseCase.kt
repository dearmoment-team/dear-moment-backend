package kr.kro.dearmoment.inquiry.application.port.input

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.author.dto.GetAuthorInquiryResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.product.dto.GetProductInquiryResponse
import kr.kro.dearmoment.inquiry.application.query.GetAuthorInquiresQuery
import kr.kro.dearmoment.inquiry.application.query.GetProductInquiresQuery

interface GetInquiryUseCase {
    fun getAuthorInquiries(query: GetAuthorInquiresQuery): PagedResponse<GetAuthorInquiryResponse>

    fun getProductInquiries(query: GetProductInquiresQuery): PagedResponse<GetProductInquiryResponse>
}
