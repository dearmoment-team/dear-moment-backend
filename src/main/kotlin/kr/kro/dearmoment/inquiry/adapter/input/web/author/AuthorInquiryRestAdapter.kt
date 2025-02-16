package kr.kro.dearmoment.inquiry.adapter.input.web.author

import kr.kro.dearmoment.inquiry.adapter.input.web.author.dto.CreateAuthorInquiryRequest
import kr.kro.dearmoment.inquiry.adapter.input.web.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.command.CreateAuthorInquiryCommand
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.RemoveInquiryUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/inquiries/authors")
class AuthorInquiryRestAdapter(
    private val createInquiryUseCase: CreateInquiryUseCase,
    private val removeInquiryUseCase: RemoveInquiryUseCase,
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

    @DeleteMapping("/{inquiryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeAuthorInquiry(
        @PathVariable inquiryId: Long,
    ): Unit = removeInquiryUseCase.removeAuthorInquiry(inquiryId)
}
