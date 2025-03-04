package kr.kro.dearmoment.studio.application.port.input

import kr.kro.dearmoment.studio.application.command.ModifyStudioCommand
import kr.kro.dearmoment.studio.application.dto.response.ModifyStudioResponse

interface ModifyStudioUseCase {
    fun modify(command: ModifyStudioCommand): ModifyStudioResponse
}
