package kr.kro.dearmoment.inquiry.application.port.output

import kr.kro.dearmoment.inquiry.domain.CreateProductOptionInquiry
import kr.kro.dearmoment.inquiry.domain.ServiceInquiry
import kr.kro.dearmoment.inquiry.domain.StudioInquiry

interface SaveInquiryPort {
    fun saveProductOptionInquiry(inquiry: CreateProductOptionInquiry): Long

    fun saveStudioInquiry(inquiry: StudioInquiry): Long

    fun saveServiceInquiry(inquiry: ServiceInquiry): Long
}
