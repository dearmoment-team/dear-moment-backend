package kr.kro.dearmoment.inquiry.application.port.input

import kr.kro.dearmoment.inquiry.adapter.input.web.author.dto.WriteAnswerResponse
import kr.kro.dearmoment.inquiry.application.command.WriteAuthorInquiryAnswerCommand

interface UpdateInquiryUseCase {
    fun writeAuthorInquiryAnswer(command: WriteAuthorInquiryAnswerCommand): WriteAnswerResponse
}
