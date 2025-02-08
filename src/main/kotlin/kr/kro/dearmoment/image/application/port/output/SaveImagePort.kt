package kr.kro.dearmoment.image.application.port.output

import kr.kro.dearmoment.image.domain.Image

interface SaveImagePort {
    fun save(image: Image): Long

    fun saveAll(images: List<Image>): List<Long>
}
