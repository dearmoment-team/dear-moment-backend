package kr.kro.dearmoment.inquiry.adapter.input.web.service

import kr.kro.dearmoment.inquiry.adapter.input.web.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.service.dto.CreateServiceInquiryRequest
import kr.kro.dearmoment.inquiry.application.command.CreateServiceInquiryCommand
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
@RequestMapping("/api/inquiries/services")
class ServiceInquiryRestAdapter(
    private val createInquiryUseCase: CreateInquiryUseCase,
    private val removeInquiryUseCase: RemoveInquiryUseCase,
) {
    @PostMapping
    fun writeServiceInquiry(
        @RequestBody request: CreateServiceInquiryRequest,
    ): CreateInquiryResponse {
        val command =
            CreateServiceInquiryCommand(
                userId = request.userId,
                type = request.type,
                content = request.content,
            )

        return createInquiryUseCase.createServiceInquiry(command)
    }

    @DeleteMapping("/{inquiryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeServiceInquiry(
        @PathVariable inquiryId: Long,
    ): Unit = removeInquiryUseCase.removeServiceInquiry(inquiryId)
}
