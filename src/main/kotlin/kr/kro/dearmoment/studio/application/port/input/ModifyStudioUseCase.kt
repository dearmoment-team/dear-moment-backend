package kr.kro.dearmoment.studio.application.port.input

import kr.kro.dearmoment.studio.application.command.ModifyStudioCommand
import kr.kro.dearmoment.studio.application.dto.response.StudioResponse

interface ModifyStudioUseCase {
    fun modify(command: ModifyStudioCommand): StudioResponse
}
