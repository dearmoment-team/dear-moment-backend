package kr.kro.dearmoment.image.application.port.output

import kr.kro.dearmoment.image.domain.Image

interface DeleteImageFromObjectStoragePort {
    fun delete(image: Image)
}
