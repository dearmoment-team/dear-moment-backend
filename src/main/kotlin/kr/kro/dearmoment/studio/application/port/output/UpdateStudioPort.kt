package kr.kro.dearmoment.studio.application.port.output

import kr.kro.dearmoment.studio.domain.Studio

interface UpdateStudioPort {
    fun update(studio: Studio): Studio
}
