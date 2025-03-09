package kr.kro.dearmoment.studio.application.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.kro.dearmoment.studio.domain.Studio

data class StudioResponse(
    @Schema(description = "스튜디오 ID", example = "1")
    val id: Long,
) {
    companion object {
        fun from(domain: Studio) = StudioResponse(id = domain.id)
    }
}
