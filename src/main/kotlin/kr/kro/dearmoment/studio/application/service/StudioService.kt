package kr.kro.dearmoment.studio.application.service

import kr.kro.dearmoment.studio.adapter.input.dto.response.ModifyStudioResponse
import kr.kro.dearmoment.studio.adapter.input.dto.response.RegisterStudioResponse
import kr.kro.dearmoment.studio.application.command.ModifyStudioCommand
import kr.kro.dearmoment.studio.application.command.RegisterStudioCommand
import kr.kro.dearmoment.studio.application.port.input.DeleteStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.ModifyStudioUseCase
import kr.kro.dearmoment.studio.application.port.input.RegisterStudioUseCase
import kr.kro.dearmoment.studio.application.port.output.DeleteStudioPort
import kr.kro.dearmoment.studio.application.port.output.SaveStudioPort
import kr.kro.dearmoment.studio.application.port.output.UpdateStudioPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudioService(
    private val saveStudioPort: SaveStudioPort,
    private val updateStudioPort: UpdateStudioPort,
    private val deleteStudioPort: DeleteStudioPort,
) : RegisterStudioUseCase, ModifyStudioUseCase, DeleteStudioUseCase {
    @Transactional
    override fun register(command: RegisterStudioCommand): RegisterStudioResponse {
        val studio = command.toDomain()
        val savedStudio = saveStudioPort.save(studio)

        return RegisterStudioResponse(
            id = savedStudio.id,
            name = savedStudio.name,
            contact = savedStudio.contact,
            studioIntro = savedStudio.studioIntro,
            artistsIntro = savedStudio.artistsIntro,
            instagramUrl = savedStudio.instagramUrl,
            kakaoChannelUrl = savedStudio.kakaoChannelUrl,
            reservationNotice = savedStudio.reservationNotice,
            cancellationPolicy = savedStudio.cancellationPolicy,
            status = savedStudio.status.name,
        )
    }

    @Transactional
    override fun modify(command: ModifyStudioCommand): ModifyStudioResponse {
        val studio = command.toDomain()
        val updatedStudio = updateStudioPort.update(studio)

        return ModifyStudioResponse(
            id = updatedStudio.id,
            name = updatedStudio.name,
            contact = updatedStudio.contact,
            studioIntro = updatedStudio.studioIntro,
            artistsIntro = updatedStudio.artistsIntro,
            instagramUrl = updatedStudio.instagramUrl,
            kakaoChannelUrl = updatedStudio.kakaoChannelUrl,
            reservationNotice = updatedStudio.reservationNotice,
            cancellationPolicy = updatedStudio.cancellationPolicy,
            status = updatedStudio.status.name,
        )
    }

    @Transactional
    override fun delete(id: Long) {
        deleteStudioPort.delete(id)
    }
}
