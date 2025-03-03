package kr.kro.dearmoment.studio.application.port.input

import kr.kro.dearmoment.studio.adapter.input.dto.response.RegisterStudioResponse
import kr.kro.dearmoment.studio.application.command.RegisterStudioCommand

interface RegisterStudioUseCase {
    fun register(command: RegisterStudioCommand): RegisterStudioResponse
}
