package kr.kro.dearmoment.inquiry.application.port.input

import kr.kro.dearmoment.inquiry.application.command.CreateAuthorInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateProductInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.CreateServiceInquiryCommand

interface CreateInquiryUseCase {
    fun createAuthorInquiry(command: CreateAuthorInquiryCommand): Long

    fun createProductInquiry(command: CreateProductInquiryCommand): Long

    fun createServiceInquiry(command: CreateServiceInquiryCommand): Long
}
