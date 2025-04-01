package kr.kro.dearmoment.inquiry.adapter.input.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.inquiry.application.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.CreateProductOptionInquiryRequest
import kr.kro.dearmoment.inquiry.application.dto.GetProductOptionInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.RemoveProductOptionInquiryRequest
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.GetInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.RemoveInquiryUseCase
import kr.kro.dearmoment.inquiry.application.query.GetProductInquiresQuery
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "ProductOptionInquiry API", description = "상품 옵션 문의 관련 API")
@RestController
@RequestMapping("/api/inquiries/product-options")
class ProductOptionInquiryRestAdapter(
    private val createInquiryUseCase: CreateInquiryUseCase,
    private val getInquiryUseCase: GetInquiryUseCase,
    private val removeInquiryUseCase: RemoveInquiryUseCase,
) {
    @Operation(summary = "상품 옵션 문의 생성", description = "새로운 상품 옵션 문의를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 옵션 문의 생성 성공",
                content = [Content(schema = Schema(implementation = CreateInquiryResponse::class))],
            ),
        ],
    )
    @PostMapping
    fun writeProductOptionInquiry(
        @Parameter(description = "생성할 상품 옵션 문의 정보", required = true)
        @Valid
        @RequestBody request: CreateProductOptionInquiryRequest,
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): CreateInquiryResponse = createInquiryUseCase.createProductOptionInquiry(request.toCommand(userId))

    @Operation(summary = "유저 상품 옵션 문의 조히", description = "유저가 등록한 상품 옵션 문의를 모두 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 옵션 문의 조회 성공",
                content = [Content(schema = Schema(implementation = PagedResponse::class))],
            ),
        ],
    )
    @GetMapping
    fun getProductOptionInquiries(
        @Parameter(description = "페이지 번호(0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): PagedResponse<GetProductOptionInquiryResponse> {
        val pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdDate")
        val query = GetProductInquiresQuery(userId, pageable)
        return getInquiryUseCase.getProductOptionInquiries(query)
    }

    @Operation(summary = "유저 상품 옵션 문의 삭제", description = "유저가 등록한 상품 옵션 문의를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "삭제 성공",
            ),
        ],
    )
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeProductOptionInquiry(
        @Parameter(description = "삭제할 상품 옵션 문의 정보", required = true)
        @RequestBody request: RemoveProductOptionInquiryRequest,
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): Unit = removeInquiryUseCase.removeProductOptionInquiry(request.toCommand(userId))
}
