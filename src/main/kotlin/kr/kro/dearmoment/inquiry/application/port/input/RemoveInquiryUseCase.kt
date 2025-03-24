package kr.kro.dearmoment.inquiry.application.port.input

import kr.kro.dearmoment.inquiry.application.command.RemoveProductOptionInquiryCommand

interface RemoveInquiryUseCase {
    fun removeProductOptionInquiry(command: RemoveProductOptionInquiryCommand)
}
