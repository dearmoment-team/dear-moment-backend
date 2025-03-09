package kr.kro.dearmoment.studio.application.port.input

import kr.kro.dearmoment.studio.application.dto.response.GetStudioResponse

interface GetStudioUseCase {
    fun getStudio(studioId: Long): GetStudioResponse
}
