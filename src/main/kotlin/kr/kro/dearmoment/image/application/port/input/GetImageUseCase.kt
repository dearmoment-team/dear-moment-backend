package kr.kro.dearmoment.image.application.port.input

import kr.kro.dearmoment.image.adapter.input.web.dto.GetImageResponse
import kr.kro.dearmoment.image.adapter.input.web.dto.GetImagesResponse
import java.util.UUID

interface GetImageUseCase {
    fun getAll(userId: UUID): GetImagesResponse

    fun getOne(imageId: Long): GetImageResponse
}
