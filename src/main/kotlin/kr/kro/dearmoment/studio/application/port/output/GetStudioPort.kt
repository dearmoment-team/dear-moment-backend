package kr.kro.dearmoment.studio.application.port.output

import kr.kro.dearmoment.studio.domain.Studio
import java.util.UUID

interface GetStudioPort {
    fun findById(id: Long): Studio

    fun findByUserId(id: UUID): Studio?
}
