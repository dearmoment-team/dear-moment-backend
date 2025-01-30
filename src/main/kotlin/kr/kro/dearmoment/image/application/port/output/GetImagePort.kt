package kr.kro.dearmoment.image.application.port.output

import kr.kro.dearmoment.image.domain.Image

interface GetImagePort {
    fun findAll(userId: Long): List<Image>

    fun findOne(imageId: Long): Image
}
