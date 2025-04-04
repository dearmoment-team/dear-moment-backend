package kr.kro.dearmoment.like.adapter.input.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.like.application.dto.GetProductLikeResponse
import kr.kro.dearmoment.like.application.dto.LikeRequest
import kr.kro.dearmoment.like.application.dto.LikeResponse
import kr.kro.dearmoment.like.application.dto.UnlikeProductRequest
import kr.kro.dearmoment.like.application.port.input.LikeQueryUseCase
import kr.kro.dearmoment.like.application.port.input.LikeUseCase
import kr.kro.dearmoment.like.application.query.GetUserProductLikeQuery
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

@Tag(name = "Like API", description = "좋아요 관련 API")
@RestController
@RequestMapping("/api/likes/products")
class ProductLikeRestAdapter(
    private val likeUseCase: LikeUseCase,
    private val likeQueryUseCase: LikeQueryUseCase,
) {
    @Operation(summary = "상품 좋아요 생성", description = "새로운 상품 좋아요를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 좋아요 생성 성공",
                content = [Content(schema = Schema(implementation = LikeResponse::class))],
            ),
        ],
    )
    @PostMapping
    fun productLike(
        @Parameter(description = "생성할 상품 좋아요 정보", required = true)
        @Valid
        @RequestBody request: LikeRequest,
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): LikeResponse = likeUseCase.productLike(request.toCommand(userId))

    @GetMapping
    fun getMyProductLikes(
        @Parameter(description = "페이지 번호(0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): PagedResponse<GetProductLikeResponse> {
        val pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdDate")
        val query = GetUserProductLikeQuery(userId, pageable)
        return likeQueryUseCase.getUserProductLikes(query)
    }

    @Operation(summary = "상품 좋아요 삭제", description = "상품 좋아요 삭제합니다.")
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
    fun productUnlike(
        @Parameter(description = "삭제할 상품 좋아요 정보", required = true)
        @RequestBody request: UnlikeProductRequest,
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): Unit = likeUseCase.productUnlike(request.toCommand(userId))
}
