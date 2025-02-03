package kr.kro.dearmoment.image.application.port.input

import kr.kro.dearmoment.image.adapter.input.web.dto.GetImageResponse
import kr.kro.dearmoment.image.adapter.input.web.dto.GetImagesResponse

interface GetImageUseCase {
    fun getAll(userId: Long): GetImagesResponse

    fun getOne(imageId: Long): GetImageResponse
}
