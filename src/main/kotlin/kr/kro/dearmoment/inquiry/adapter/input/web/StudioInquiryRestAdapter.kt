package kr.kro.dearmoment.inquiry.adapter.input.web

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.inquiry.application.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.CreateStudioInquiryRequest
import kr.kro.dearmoment.inquiry.application.dto.GetStudioInquiryResponse
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.GetInquiryUseCase
import kr.kro.dearmoment.inquiry.application.query.GetStudioInquiresQuery
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/inquiries/studios")
class StudioInquiryRestAdapter(
    private val createInquiryUseCase: CreateInquiryUseCase,
    private val getInquiryUseCase: GetInquiryUseCase,
) {
    @PostMapping
    fun writeStudioInquiry(
        @RequestBody request: CreateStudioInquiryRequest,
    ): CreateInquiryResponse = createInquiryUseCase.createStudioInquiry(request.toCommand())

    @GetMapping("/{userId}")
    fun getStudioInquiries(
        @PathVariable userId: Long,
        @PageableDefault(size = 10, sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): PagedResponse<GetStudioInquiryResponse> {
        val query = GetStudioInquiresQuery(userId, pageable)
        return getInquiryUseCase.getStudioInquiries(query)
    }
}
