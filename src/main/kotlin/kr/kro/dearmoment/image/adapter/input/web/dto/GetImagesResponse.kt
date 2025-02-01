package kr.kro.dearmoment.image.adapter.input.web.dto

import kr.kro.dearmoment.image.domain.Image

data class GetImagesResponse(val images: List<GetImageResponse>) {
    companion object {
        fun from(images: List<Image>) = GetImagesResponse(images.map { GetImageResponse.from(it) })
    }
}
