package kr.kro.dearmoment.inquiry.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.inquiry.domain.ProductOptionInquiry
import java.time.LocalDateTime

data class GetProductOptionInquiryResponse(
    @Schema(description = "문의 ID", example = "1")
    val inquiryId: Long,
    @Schema(description = "상품 ID", example = "1")
    val productId: Long,
    @Schema(description = "스튜디오 이름", example = "디어모먼트 ㅡ튜디오")
    val studioName: String,
    @Schema(description = "상품 옵션 이름", example = "Basic")
    val optionName: String,
    @Schema(description = "썸네일 url", example = "1")
    val thumbnailUrl: String,
    @Schema(description = "생성 날짜", example = "2025-03-09T15:45:30")
    val createdDate: LocalDateTime,
) {
    companion object {
        fun from(inquiry: ProductOptionInquiry) =
            GetProductOptionInquiryResponse(
                inquiryId = inquiry.id,
                productId = 0,
                studioName = inquiry.studioName,
                optionName = inquiry.optionName,
                thumbnailUrl = inquiry.thumbnailUrl,
                createdDate = inquiry.createdDate,
            )
    }
}
