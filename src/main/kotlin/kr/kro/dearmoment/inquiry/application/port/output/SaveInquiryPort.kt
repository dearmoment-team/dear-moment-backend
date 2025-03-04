package kr.kro.dearmoment.inquiry.application.port.output

import kr.kro.dearmoment.inquiry.domain.ArtistInquiry
import kr.kro.dearmoment.inquiry.domain.ProductInquiry
import kr.kro.dearmoment.inquiry.domain.ServiceInquiry

interface SaveInquiryPort {
    fun saveProductInquiry(inquiry: ProductInquiry): Long

    fun saveArtistInquiry(inquiry: ArtistInquiry): Long

    fun saveServiceInquiry(inquiry: ServiceInquiry): Long
}
