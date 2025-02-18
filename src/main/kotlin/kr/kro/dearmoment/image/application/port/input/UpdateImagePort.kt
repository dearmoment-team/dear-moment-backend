package kr.kro.dearmoment.image.application.port.input

import kr.kro.dearmoment.image.domain.Image

interface UpdateImagePort {
    fun update(image: Image): Int
}
