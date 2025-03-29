package kr.kro.dearmoment.product.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import kr.kro.dearmoment.image.domain.Image
import java.time.LocalDateTime
import java.util.UUID

@Embeddable
class ImageEmbeddable(
    @Column(name = "IMAGE_ID")
    var imageId: Long = 0L,
    @Column(name = "USER_ID")
    var userId: UUID = UUID.randomUUID(),
    @Column(name = "PAR_ID")
    var parId: String? = "",
    @Column(name = "FILE_NAME")
    var fileName: String = "",
    @Column(name = "URL")
    var url: String = "",
    @Column(name = "URL_EXPIRE_TIME")
    var urlExpireTime: LocalDateTime? = null,
) {
    companion object {
        fun fromDomainImage(image: Image): ImageEmbeddable {
            return ImageEmbeddable(
                imageId = image.imageId,
                userId = image.userId,
                parId = image.parId,
                fileName = image.fileName,
                url = image.url,
                urlExpireTime = image.urlExpireTime,
            )
        }
    }

    fun toDomainImage(): Image {
        return Image(
            imageId = this.imageId,
            userId = this.userId,
            parId = this.parId ?: "",
            fileName = this.fileName,
            url = this.url,
            urlExpireTime = this.urlExpireTime ?: LocalDateTime.now(),
        )
    }
}
