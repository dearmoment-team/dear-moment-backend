package kr.kro.dearmoment.inquiry.adapter.input.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(name = "StudioInquiry API", description = "스튜디오 문의 관련 API")
@RestController
@RequestMapping("/api/inquiries/studios")
class StudioInquiryRestAdapter(
    private val createInquiryUseCase: CreateInquiryUseCase,
    private val getInquiryUseCase: GetInquiryUseCase,
) {
    @Operation(summary = "스튜디오 문의 생성", description = "새로운 스튜디오 문의를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 옵션 문의 생성 성공",
                content = [Content(schema = Schema(implementation = CreateStudioInquiryRequest::class))],
            ),
        ],
    )
    @PostMapping
    fun writeStudioInquiry(
        @Parameter(description = "생성할 스튜디오 문의 정보", required = true)
        @RequestBody request: CreateStudioInquiryRequest,
    ): CreateInquiryResponse = createInquiryUseCase.createStudioInquiry(request.toCommand())

    @Operation(summary = "유저 스튜디오 문의 조회", description = "유저가 등록한 스튜디오 문의를 모두 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 스튜디오 조회 성공",
                content = [Content(schema = Schema(implementation = PagedResponse::class))],
            ),
        ],
    )
    @GetMapping("/{userId}")
    fun getStudioInquiries(
        @Parameter(description = "조회할 유저의 식별자", required = true)
        @PathVariable userId: Long,
        @Parameter(
            description = "페이징 정보",
            example = """{
              "page": 0,
              "size": 10,
              "sort": "createdDate",
              "direction": "DESC"
            }""",
            required = true,
        )
        @PageableDefault(size = 10, sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): PagedResponse<GetStudioInquiryResponse> {
        val query = GetStudioInquiresQuery(userId, pageable)
        return getInquiryUseCase.getStudioInquiries(query)
    }
}
