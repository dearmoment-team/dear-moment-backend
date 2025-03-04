package kr.kro.dearmoment.like.adapter.input.web

import kr.kro.dearmoment.like.application.command.SaveLikeCommand
import kr.kro.dearmoment.like.application.dto.GetLikeResponse
import kr.kro.dearmoment.like.application.dto.LikeRequest
import kr.kro.dearmoment.like.application.dto.LikeResponse
import kr.kro.dearmoment.like.application.port.input.LikeUseCase
import kr.kro.dearmoment.like.application.query.GetLikesQuery
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
) {
    @PostMapping
    fun like(
        @RequestBody request: LikeRequest,
    ): LikeResponse {
        val command =
            SaveLikeCommand(
                userId = request.userId,
                targetId = request.targetId,
                type = request.type,
            )
        return likeUseCase.like(command)
    }

    @GetMapping("/{userId}/{type}")
    fun getLikes(
        @PathVariable userId: Long,
        @PathVariable type: String,
    ): List<GetLikeResponse> = likeUseCase.getLikes(GetLikesQuery(userId, type))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unlike(
        @PathVariable id: Long,
    ) {
        likeUseCase.unlike(id)
    }
}
