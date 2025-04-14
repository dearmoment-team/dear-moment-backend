package kr.kro.dearmoment.image.adapter.output.objectstorage.event

import kr.kro.dearmoment.image.domain.Image

data class ImageDeleteEvent(
    val fileName: String,
    val parId: String,
) {
    companion object {
        fun from(image: Image) =
            ImageDeleteEvent(
                fileName = image.fileName,
                parId = image.parId
            )
    }
}
