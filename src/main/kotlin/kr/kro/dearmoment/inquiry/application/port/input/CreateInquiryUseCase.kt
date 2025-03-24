package kr.kro.dearmoment.inquiry.application.port.input

import kr.kro.dearmoment.inquiry.application.command.CreateProductOptionInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateServiceInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateStudioInquiryCommand
import kr.kro.dearmoment.inquiry.application.dto.CreateInquiryResponse

interface CreateInquiryUseCase {
    fun createStudioInquiry(command: CreateStudioInquiryCommand): CreateInquiryResponse

    fun createProductOptionInquiry(command: CreateProductOptionInquiryCommand): CreateInquiryResponse

    fun createServiceInquiry(command: CreateServiceInquiryCommand): CreateInquiryResponse
}
