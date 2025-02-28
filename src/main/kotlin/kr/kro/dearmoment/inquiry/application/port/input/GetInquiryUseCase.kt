package kr.kro.dearmoment.inquiry.application.port.input

import kr.kro.dearmoment.inquiry.adapter.input.web.author.dto.GetAuthorInquiriesResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.product.dto.GetProductInquiriesResponse

interface GetInquiryUseCase {
    fun getAuthorInquiries(userId: Long): GetAuthorInquiriesResponse

    fun getProductInquiries(userId: Long): GetProductInquiriesResponse
}
