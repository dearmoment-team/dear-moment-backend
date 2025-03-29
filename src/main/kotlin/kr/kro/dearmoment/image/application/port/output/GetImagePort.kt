package kr.kro.dearmoment.image.application.port.output

import kr.kro.dearmoment.image.domain.Image
import java.util.UUID

interface GetImagePort {
    fun findUserImages(userId: UUID): List<Image>

    fun findOne(imageId: Long): Image
}
