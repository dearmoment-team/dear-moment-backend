package kr.kro.dearmoment.studio.application.port.output

import kr.kro.dearmoment.studio.domain.Studio

interface GetStudioPort {
    fun findById(id: Long): Studio
}
