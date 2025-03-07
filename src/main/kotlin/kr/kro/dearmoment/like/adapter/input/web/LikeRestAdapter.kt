package kr.kro.dearmoment.like.adapter.input.web

import kr.kro.dearmoment.like.application.dto.GetProductOptionLikeResponse
import kr.kro.dearmoment.like.application.dto.GetStudioLikeResponse
import kr.kro.dearmoment.like.application.dto.LikeRequest
import kr.kro.dearmoment.like.application.dto.LikeResponse
import kr.kro.dearmoment.like.application.port.input.LikeQueryUseCase
import kr.kro.dearmoment.like.application.port.input.LikeUseCase
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
@RequestMapping("/api/likes")
class LikeRestAdapter(
    private val likeUseCase: LikeUseCase,
    private val likeQueryUseCase: LikeQueryUseCase,
) {
    @PostMapping("/studios")
    fun studioLike(
        @RequestBody request: LikeRequest,
    ): LikeResponse = likeUseCase.studioLike(request.toCommand())

    @PostMapping("/product-options")
    fun productOptionLike(
        @RequestBody request: LikeRequest,
    ): LikeResponse = likeUseCase.productOptionsLike(request.toCommand())

    @GetMapping("/studios/{userId}")
    fun getMyStudioLikes(
        @PathVariable userId: Long,
    ): List<GetStudioLikeResponse> = likeQueryUseCase.getUserStudioLikes(userId)

    @GetMapping("/product-options/{userId}")
    fun getMyProductOptionLikes(
        @PathVariable userId: Long,
    ): List<GetProductOptionLikeResponse> = likeQueryUseCase.getUserProductOptionLikes(userId)

    @DeleteMapping("/studios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun studioUnlike(
        @PathVariable id: Long,
    ) {
        likeUseCase.studioUnlike(id)
    }

    @DeleteMapping("/product-options/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun productOptionUnlike(
        @PathVariable id: Long,
    ) {
        likeUseCase.productOptionUnlike(id)
    }
}
