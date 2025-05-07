package kr.kro.dearmoment.studio.adapter.input

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.kro.dearmoment.studio.application.dto.request.ModifyStudioRequest
import kr.kro.dearmoment.studio.application.dto.request.RegisterStudioRequest
import kr.kro.dearmoment.studio.application.dto.response.GetStudioResponse
import kr.kro.dearmoment.studio.application.dto.response.StudioResponse
import kr.kro.dearmoment.studio.application.port.input.DeleteStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.GetStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.ModifyStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.RegisterStudioUseCase
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Studio API", description = "스튜디오 관련 API")
@RestController
@RequestMapping("/api/studios")
class StudioRestAdapter(
    private val getStudioUseCase: GetStudioUseCase,
    private val registerStudioUseCase: RegisterStudioUseCase,
    private val modifyStudioUseCase: ModifyStudioUseCase,
    private val deleteStudioUseCase: DeleteStudioUseCase,
) {
    @Operation(summary = "스튜디오 생성", description = "새로운 스튜디오를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "스튜디오 생성 성공",
                content = [Content(schema = Schema(implementation = StudioResponse::class))],
            ),
        ],
    )
    @PostMapping
    fun register(
        @Parameter(description = "생성할 스튜디오 정보", required = true)
        @Valid
        @RequestBody request: RegisterStudioRequest,
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): StudioResponse = registerStudioUseCase.register(request.toCommand(userId))

    @Operation(summary = "스튜디오 수정", description = "스튜디오를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "스튜디오 수정 성공",
                content = [Content(schema = Schema(implementation = StudioResponse::class))],
            ),
        ],
    )
    @PutMapping("/{studioId}")
    fun modify(
        @Parameter(description = "수정할 스튜디오 식별자", required = true)
        @PathVariable studioId: Long,
        @Parameter(description = "수정할 스튜디오 정보", required = true)
        @Valid
        @RequestBody request: ModifyStudioRequest,
        @AuthenticationPrincipal(expression = "id") userId: UUID,
    ): StudioResponse = modifyStudioUseCase.modify(request.toCommand(studioId, userId))

    @Operation(summary = "스튜디오 단건 조회", description = "스튜디오 1개를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "스튜디오 조회 성공",
                content = [Content(schema = Schema(implementation = GetStudioResponse::class))],
            ),
        ],
    )
    @GetMapping("/{studioId}")
    fun getStudio(
        @Parameter(description = "조회할 스튜디오 식별자", required = true)
        @PathVariable studioId: Long,
    ): GetStudioResponse = getStudioUseCase.getStudio(studioId)

    @Operation(summary = "스튜디오 삭제", description = "스튜디오를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "삭제 성공",
            ),
        ],
    )
    @DeleteMapping("/{studioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(
        @Parameter(description = "삭제할 스튜디오 식별자", required = true)
        @PathVariable studioId: Long,
    ): Unit = deleteStudioUseCase.delete(studioId)
}
