package kr.kro.dearmoment.image.adapter.input.web.dto

import kr.kro.dearmoment.image.domain.Image

data class GetImageResponse(
    val imageId: Long,
    val fileName: String,
) {
    companion object {
        fun from(image: Image): GetImageResponse =
            GetImageResponse(
                imageId = image.imageId,
                fileName = image.fileName,
            )
    }
}
