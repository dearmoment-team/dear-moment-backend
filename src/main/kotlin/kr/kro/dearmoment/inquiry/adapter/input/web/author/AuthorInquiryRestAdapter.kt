package kr.kro.dearmoment.inquiry.adapter.input.web.author

import kr.kro.dearmoment.inquiry.adapter.input.web.author.dto.CreateAuthorInquiryRequest
import kr.kro.dearmoment.inquiry.adapter.input.web.author.dto.GetAuthorInquiriesResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.author.dto.WriteAnswerRequest
import kr.kro.dearmoment.inquiry.adapter.input.web.author.dto.WriteAnswerResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.command.CreateAuthorInquiryCommand
import kr.kro.dearmoment.inquiry.application.command.WriteAuthorInquiryAnswerCommand
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.GetInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.UpdateInquiryUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/inquiries/authors")
class AuthorInquiryRestAdapter(
    private val createInquiryUseCase: CreateInquiryUseCase,
    private val getInquiryUseCase: GetInquiryUseCase,
    private val updateInquiryUseCase: UpdateInquiryUseCase,
) {
    @PostMapping
    fun writeAuthorInquiry(
        @RequestBody request: CreateAuthorInquiryRequest,
    ): CreateInquiryResponse {
        val command =
            CreateAuthorInquiryCommand(
                userId = request.userId,
                title = request.title,
                content = request.content,
            )

        return createInquiryUseCase.createAuthorInquiry(command)
    }

    @GetMapping("/{userId}")
    fun getAuthorInquiries(
        @PathVariable userId: Long,
    ): GetAuthorInquiriesResponse = getInquiryUseCase.getAuthorInquiries(userId)

    @PutMapping("/{inquiryId}")
    fun writeAnswer(
        @PathVariable inquiryId: Long,
        @RequestBody request: WriteAnswerRequest,
    ): WriteAnswerResponse {
        val command = WriteAuthorInquiryAnswerCommand(inquiryId, request.answer)
        return updateInquiryUseCase.writeAuthorInquiryAnswer(command)
    }
}
