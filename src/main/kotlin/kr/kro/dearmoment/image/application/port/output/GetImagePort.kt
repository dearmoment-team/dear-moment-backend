package kr.kro.dearmoment.image.application.port.output

import kr.kro.dearmoment.image.domain.Image

interface GetImagePort {
    fun findUserImages(userId: Long): List<Image>

    fun findOne(imageId: Long): Image
}
