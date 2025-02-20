package kr.kro.dearmoment.image.application.port.input

import kr.kro.dearmoment.image.domain.Image

interface UpdateImagePort {
    fun updateUrlInfo(image: Image): Image
}
