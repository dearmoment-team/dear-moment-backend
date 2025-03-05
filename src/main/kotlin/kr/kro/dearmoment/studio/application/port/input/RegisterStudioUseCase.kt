package kr.kro.dearmoment.studio.application.port.input

import kr.kro.dearmoment.studio.application.command.RegisterStudioCommand
import kr.kro.dearmoment.studio.application.dto.response.StudioResponse

interface RegisterStudioUseCase {
    fun register(command: RegisterStudioCommand): StudioResponse
}
