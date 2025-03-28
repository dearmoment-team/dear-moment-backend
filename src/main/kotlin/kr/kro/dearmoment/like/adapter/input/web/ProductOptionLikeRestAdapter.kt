package kr.kro.dearmoment.like.adapter.input.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.like.application.dto.GetProductOptionLikeResponse
import kr.kro.dearmoment.like.application.dto.LikeRequest
import kr.kro.dearmoment.like.application.dto.LikeResponse
import kr.kro.dearmoment.like.application.dto.UnlikeProductOptionRequest
import kr.kro.dearmoment.like.application.port.input.LikeQueryUseCase
import kr.kro.dearmoment.like.application.port.input.LikeUseCase
import kr.kro.dearmoment.like.application.query.GetUserProductOptionLikeQuery
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Product Option Like API", description = "상품 옵션 좋아요 관련 API")
@RestController
@RequestMapping("/api/likes/product-options")
class ProductOptionLikeRestAdapter(
    private val likeUseCase: LikeUseCase,
    private val likeQueryUseCase: LikeQueryUseCase,
) {
    @Operation(summary = "상품 옵션 좋아요 생성", description = "새로운 상품 옵션 좋아요를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "상품 옵션 생성 성공",
                content = [Content(schema = Schema(implementation = LikeResponse::class))],
            ),
        ],
    )
    @PostMapping
    fun productOptionLike(
        @Parameter(description = "생성할 상품 옵션 좋아요 정보", required = true)
        @RequestBody request: LikeRequest,
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): LikeResponse = likeUseCase.productOptionsLike(request.toCommand(userId))

    @Operation(summary = "나의 상품 옵션 좋아요 조회", description = "유저의 상풉 옵션 좋아요들을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = PagedResponse::class))],
            ),
        ],
    )
    @GetMapping()
    fun getMyProductOptionLikes(
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
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): PagedResponse<GetProductOptionLikeResponse> {
        val query = GetUserProductOptionLikeQuery(userId, pageable)
        return likeQueryUseCase.getUserProductOptionLikes(query)
    }

    @Operation(summary = "상품 옵션 좋아요 삭제", description = "상품 옵션 좋아요를 삭제합니다.")
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
    fun productOptionUnlike(
        @Parameter(description = "삭제할 상품 좋아요 정보", required = true)
        @RequestBody request: UnlikeProductOptionRequest,
    ): Unit = likeUseCase.productOptionUnlike(request.toCommand())
}
