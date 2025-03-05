package kr.kro.dearmoment.studio.application.port.output

import kr.kro.dearmoment.studio.domain.Studio

interface SaveStudioPort {
    fun save(studio: Studio): Studio
}
