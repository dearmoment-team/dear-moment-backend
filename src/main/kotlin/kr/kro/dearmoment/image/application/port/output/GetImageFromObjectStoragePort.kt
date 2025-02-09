package kr.kro.dearmoment.image.application.port.output

import kr.kro.dearmoment.image.domain.Image

interface GetImageFromObjectStoragePort {
    fun getImage(image: Image): Image
}
