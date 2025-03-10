package kr.kro.dearmoment.inquiry.adapter.input.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.kro.dearmoment.inquiry.application.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.CreateServiceInquiryRequest
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "ServiceInquiry API", description = "서비스 문의 관련 API")
@RestController
@RequestMapping("/api/inquiries/services")
class ServiceInquiryRestAdapter(
    private val createInquiryUseCase: CreateInquiryUseCase,
) {
    @Operation(summary = "서비스 문의 생성", description = "새로운 서비스 문의를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "서비스 문의 생성 성공",
                content = [Content(schema = Schema(implementation = CreateInquiryResponse::class))],
            ),
        ],
    )
    @PostMapping
    fun writeServiceInquiry(
        @Parameter(description = "생성할 서비스 문의 정보", required = true)
        @RequestBody request: CreateServiceInquiryRequest,
    ): CreateInquiryResponse = createInquiryUseCase.createServiceInquiry(request.toCommand())
}
