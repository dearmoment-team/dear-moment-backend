package kr.kro.dearmoment.studio.application.dto.response

import kr.kro.dearmoment.studio.domain.Studio

data class StudioResponse(
    val id: Long,
) {
    companion object {
        fun from(domain: Studio) = StudioResponse(id = domain.id)
    }
}
