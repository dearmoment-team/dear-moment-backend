package kr.kro.dearmoment.inquiry.adapter.input.web

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.inquiry.application.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.CreateProductOptionInquiryRequest
import kr.kro.dearmoment.inquiry.application.dto.GetProductOptionInquiryResponse
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.GetInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.RemoveInquiryUseCase
import kr.kro.dearmoment.inquiry.application.query.GetProductInquiresQuery
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/inquiries/product-options")
class ProductOptionInquiryRestAdapter(
    private val createInquiryUseCase: CreateInquiryUseCase,
    private val getInquiryUseCase: GetInquiryUseCase,
    private val removeInquiryUseCase: RemoveInquiryUseCase,
) {
    @PostMapping
    fun writeProductOptionInquiry(
        @RequestBody request: CreateProductOptionInquiryRequest,
    ): CreateInquiryResponse = createInquiryUseCase.createProductInquiry(request.toCommand())

    @GetMapping("/{userId}")
    fun getProductOptionInquiries(
        @PathVariable userId: Long,
        @PageableDefault(size = 10, sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): PagedResponse<GetProductOptionInquiryResponse> {
        val query = GetProductInquiresQuery(userId, pageable)
        return getInquiryUseCase.getProductOptionInquiries(query)
    }

    @DeleteMapping("/{inquiryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeProductOptionInquiry(
        @PathVariable inquiryId: Long,
    ): Unit = removeInquiryUseCase.removeProductInquiry(inquiryId)
}
