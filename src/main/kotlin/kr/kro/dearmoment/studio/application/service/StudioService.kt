package kr.kro.dearmoment.studio.application.service

import kr.kro.dearmoment.studio.application.command.ModifyStudioCommand
import kr.kro.dearmoment.studio.application.command.RegisterStudioCommand
import kr.kro.dearmoment.studio.application.dto.response.GetStudioResponse
import kr.kro.dearmoment.studio.application.dto.response.StudioResponse
import kr.kro.dearmoment.studio.application.port.input.DeleteStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.GetStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.ModifyStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.RegisterStudioUseCase
import kr.kro.dearmoment.studio.application.port.output.DeleteStudioPort
import kr.kro.dearmoment.studio.application.port.output.GetStudioPort
import kr.kro.dearmoment.studio.application.port.output.SaveStudioPort
import kr.kro.dearmoment.studio.application.port.output.UpdateStudioPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudioService(
    private val saveStudioPort: SaveStudioPort,
    private val getStudioPort: GetStudioPort,
    private val updateStudioPort: UpdateStudioPort,
    private val deleteStudioPort: DeleteStudioPort,
) : RegisterStudioUseCase, GetStudioUseCase, ModifyStudioUseCase, DeleteStudioUseCase {
    @Transactional
    override fun register(command: RegisterStudioCommand): StudioResponse {
        val studio = command.toDomain()

        return StudioResponse.from(saveStudioPort.save(studio))
    }

    override fun getStudio(studioId: Long): GetStudioResponse {
        val studio = getStudioPort.findById(studioId)

        return GetStudioResponse.from(studio)
    }

    @Transactional
    override fun modify(command: ModifyStudioCommand): StudioResponse {
        val studio = command.toDomain()
        val updatedStudio = updateStudioPort.update(studio)

        return StudioResponse.from(updatedStudio)
    }

    @Transactional
    override fun delete(id: Long) {
        deleteStudioPort.delete(id)
    }
}
