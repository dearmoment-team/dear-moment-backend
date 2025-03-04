package kr.kro.dearmoment.inquiry.application.port.input

import kr.kro.dearmoment.inquiry.application.command.CreateArtistInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateProductInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateServiceInquiryCommand
import kr.kro.dearmoment.inquiry.application.dto.CreateInquiryResponse

interface CreateInquiryUseCase {
    fun createArtistInquiry(command: CreateArtistInquiryCommand): CreateInquiryResponse

    fun createProductInquiry(command: CreateProductInquiryCommand): CreateInquiryResponse

    fun createServiceInquiry(command: CreateServiceInquiryCommand): CreateInquiryResponse
}
