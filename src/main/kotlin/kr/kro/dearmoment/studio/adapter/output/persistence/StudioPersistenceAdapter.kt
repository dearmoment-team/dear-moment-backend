package kr.kro.dearmoment.studio.adapter.output.persistence

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.studio.application.port.output.DeleteStudioPort
import kr.kro.dearmoment.studio.application.port.output.SaveStudioPort
import kr.kro.dearmoment.studio.application.port.output.UpdateStudioPort
import kr.kro.dearmoment.studio.domain.Studio
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class StudioPersistenceAdapter(
    private val studioJpaRepository: StudioJpaRepository,
) : SaveStudioPort, UpdateStudioPort, DeleteStudioPort {
    override fun save(studio: Studio): Studio {
        val entity = StudioEntity.from(studio)
        return studioJpaRepository.save(entity).toDomain()
    }

    override fun update(studio: Studio): Studio {
        val entity =
            studioJpaRepository.findByIdOrNull(studio.id)
                ?: throw CustomException(ErrorCode.STUDIO_NOT_FOUND)
        entity.update(StudioEntity.from(studio))

        return entity.toDomain()
    }

    override fun delete(id: Long) {
        studioJpaRepository.deleteById(id)
    }
}
