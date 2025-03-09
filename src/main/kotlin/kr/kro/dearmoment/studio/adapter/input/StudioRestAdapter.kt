package kr.kro.dearmoment.studio.adapter.input

import kr.kro.dearmoment.studio.application.dto.request.ModifyStudioRequest
import kr.kro.dearmoment.studio.application.dto.request.RegisterStudioRequest
import kr.kro.dearmoment.studio.application.dto.response.GetStudioResponse
import kr.kro.dearmoment.studio.application.dto.response.StudioResponse
import kr.kro.dearmoment.studio.application.port.input.DeleteStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.GetStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.ModifyStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.RegisterStudioUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/studios")
class StudioRestAdapter(
    private val getStudioUseCase: GetStudioUseCase,
    private val registerStudioUseCase: RegisterStudioUseCase,
    private val modifyStudioUseCase: ModifyStudioUseCase,
    private val deleteStudioUseCase: DeleteStudioUseCase,
) {
    @PostMapping
    fun register(
        @RequestBody request: RegisterStudioRequest,
    ): StudioResponse = registerStudioUseCase.register(request.toCommand())

    @PutMapping("/{studioId}")
    fun modify(
        @PathVariable studioId: Long,
        @RequestBody request: ModifyStudioRequest,
    ): StudioResponse = modifyStudioUseCase.modify(request.toCommand(studioId))

    @GetMapping("/{studioId}")
    fun getStudio(
        @PathVariable studioId: Long,
    ): GetStudioResponse = getStudioUseCase.getStudio(studioId)

    @DeleteMapping("/{studioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(
        @PathVariable studioId: Long,
    ): Unit = deleteStudioUseCase.delete(studioId)
}
