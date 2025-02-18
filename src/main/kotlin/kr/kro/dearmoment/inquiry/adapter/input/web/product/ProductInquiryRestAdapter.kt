package kr.kro.dearmoment.inquiry.adapter.input.web.product

import kr.kro.dearmoment.inquiry.adapter.input.web.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.adapter.input.web.product.dto.CreateProductInquiryRequest
import kr.kro.dearmoment.inquiry.adapter.input.web.product.dto.GetProductInquiriesResponse
import kr.kro.dearmoment.inquiry.application.command.CreateProductInquiryCommand
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.GetInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.RemoveInquiryUseCase
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
@RequestMapping("/api/inquiries/products")
class ProductInquiryRestAdapter(
    private val createInquiryUseCase: CreateInquiryUseCase,
    private val getInquiryUseCase: GetInquiryUseCase,
    private val removeInquiryUseCase: RemoveInquiryUseCase,
) {
    @PostMapping
    fun writeProductInquiry(
        @RequestBody request: CreateProductInquiryRequest,
    ): CreateInquiryResponse {
        val command =
            CreateProductInquiryCommand(
                userId = request.userId,
                productId = request.productId,
            )

        return createInquiryUseCase.createProductInquiry(command)
    }

    @GetMapping("/{userId}")
    fun getProductInquiries(
        @PathVariable userId: Long,
    ): GetProductInquiriesResponse = getInquiryUseCase.getProductInquiries(userId)

    @DeleteMapping("/{inquiryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeProductInquiry(
        @PathVariable inquiryId: Long,
    ): Unit = removeInquiryUseCase.removeProductInquiry(inquiryId)
}
