package kr.kro.dearmoment.image.application.port.output

import kr.kro.dearmoment.image.domain.Image

interface SaveImagePort {
    fun save(image: Image): Image

    fun saveAll(images: List<Image>): List<Long>
}
