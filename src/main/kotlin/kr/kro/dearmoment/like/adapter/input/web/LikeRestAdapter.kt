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
import kr.kro.dearmoment.like.application.dto.GetStudioLikeResponse
import kr.kro.dearmoment.like.application.dto.LikeRequest
import kr.kro.dearmoment.like.application.dto.LikeResponse
import kr.kro.dearmoment.like.application.port.input.LikeQueryUseCase
import kr.kro.dearmoment.like.application.port.input.LikeUseCase
import kr.kro.dearmoment.like.application.query.GetUserProductOptionLikeQuery
import kr.kro.dearmoment.like.application.query.GetUserStudioLikeQuery
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

@Tag(name = "Like API", description = "좋아요 관련 API")
@RestController
@RequestMapping("/api/likes")
class LikeRestAdapter(
    private val likeUseCase: LikeUseCase,
    private val likeQueryUseCase: LikeQueryUseCase,
) {
    @Operation(summary = "스튜디오 좋아요 생성", description = "새로운 스튜디오 좋아요를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "스튜디오 좋아요 생성 성공",
                content = [Content(schema = Schema(implementation = LikeResponse::class))],
            ),
        ],
    )
    @PostMapping("/studios")
    fun studioLike(
        @Parameter(description = "생성할 스튜디오 좋아요 정보", required = true)
        @RequestBody request: LikeRequest,
    ): LikeResponse = likeUseCase.studioLike(request.toCommand())

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
    @PostMapping("/product-options")
    fun productOptionLike(
        @Parameter(description = "생성할 상품 옵션 좋아요 정보", required = true)
        @RequestBody request: LikeRequest,
    ): LikeResponse = likeUseCase.productOptionsLike(request.toCommand())

    @Operation(summary = "나의 스튜디오 좋아요 조회", description = "유저의 스튜디오 좋아요들을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = PagedResponse::class))],
            ),
        ],
    )
    @GetMapping("/studios/{userId}")
    fun getMyStudioLikes(
        @Parameter(description = "유저 식별자", required = true)
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
    ): PagedResponse<GetStudioLikeResponse> {
        val query = GetUserStudioLikeQuery(userId, pageable)
        return likeQueryUseCase.getUserStudioLikes(query)
    }

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
    @GetMapping("/product-options/{userId}")
    fun getMyProductOptionLikes(
        @Parameter(description = "유저 식별자", required = true)
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
    ): PagedResponse<GetProductOptionLikeResponse> {
        val query = GetUserProductOptionLikeQuery(userId, pageable)
        return likeQueryUseCase.getUserProductOptionLikes(query)
    }

    @Operation(summary = "스튜디오 좋아요 삭제", description = "스튜디오를 좋아요 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "삭제 성공",
            ),
        ],
    )
    @DeleteMapping("/studios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun studioUnlike(
        @Parameter(description = "삭제할 스튜디오 좋아요 식별자", required = true)
        @PathVariable id: Long,
    ) {
        likeUseCase.studioUnlike(id)
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
    @DeleteMapping("/product-options/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun productOptionUnlike(
        @Parameter(description = "삭제할 상품 옵션 좋아요 식별자", required = true)
        @PathVariable id: Long,
    ) {
        likeUseCase.productOptionUnlike(id)
    }
}
