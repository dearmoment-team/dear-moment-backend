package kr.kro.dearmoment.inquiry.application.port.input

import kr.kro.dearmoment.inquiry.adapter.input.web.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.command.CreateAuthorInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateProductInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateServiceInquiryCommand

interface CreateInquiryUseCase {
    fun createAuthorInquiry(command: CreateAuthorInquiryCommand): CreateInquiryResponse

    fun createProductInquiry(command: CreateProductInquiryCommand): CreateInquiryResponse

    fun createServiceInquiry(command: CreateServiceInquiryCommand): CreateInquiryResponse
}
