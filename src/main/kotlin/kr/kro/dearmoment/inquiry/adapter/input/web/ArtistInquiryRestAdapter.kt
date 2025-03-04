package kr.kro.dearmoment.inquiry.adapter.input.web

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.inquiry.application.command.CreateArtistInquiryCommand
import kr.kro.dearmoment.inquiry.application.dto.CreateArtistInquiryRequest
import kr.kro.dearmoment.inquiry.application.dto.CreateInquiryResponse
import kr.kro.dearmoment.inquiry.application.dto.GetArtistInquiryResponse
import kr.kro.dearmoment.inquiry.application.port.input.CreateInquiryUseCase
import kr.kro.dearmoment.inquiry.application.port.input.GetInquiryUseCase
import kr.kro.dearmoment.inquiry.application.query.GetArtistInquiresQuery
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
@RequestMapping("/api/inquiries/artists")
class ArtistInquiryRestAdapter(
    private val createInquiryUseCase: CreateInquiryUseCase,
    private val getInquiryUseCase: GetInquiryUseCase,
) {
    @PostMapping
    fun writeArtistInquiry(
        @RequestBody request: CreateArtistInquiryRequest,
    ): CreateInquiryResponse {
        val command =
            CreateArtistInquiryCommand(
                userId = request.userId,
                title = request.title,
                content = request.content,
                email = request.email,
            )

        return createInquiryUseCase.createArtistInquiry(command)
    }

    @GetMapping("/{userId}")
    fun getArtistInquiries(
        @PathVariable userId: Long,
        @PageableDefault(size = 10, sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): PagedResponse<GetArtistInquiryResponse> {
        val query = GetArtistInquiresQuery(userId, pageable)
        return getInquiryUseCase.getArtistInquiries(query)
    }
}
