package kr.kro.dearmoment.studio.application.port.input

import kr.kro.dearmoment.studio.adapter.input.dto.response.ModifyStudioResponse
import kr.kro.dearmoment.studio.application.command.ModifyStudioCommand

interface ModifyStudioUseCase {
    fun modify(command: ModifyStudioCommand): ModifyStudioResponse
}
