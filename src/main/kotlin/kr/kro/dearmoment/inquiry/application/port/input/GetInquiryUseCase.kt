package kr.kro.dearmoment.inquiry.application.port.input

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.inquiry.application.dto.GetArtistInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.GetProductInquiryResponse
import kr.kro.dearmoment.inquiry.application.query.GetArtistInquiresQuery
import kr.kro.dearmoment.inquiry.application.query.GetProductInquiresQuery

interface GetInquiryUseCase {
    fun getArtistInquiries(query: GetArtistInquiresQuery): PagedResponse<GetArtistInquiryResponse>

    fun getProductInquiries(query: GetProductInquiresQuery): PagedResponse<GetProductInquiryResponse>
}
