package kr.kro.dearmoment.image.adapter.output.persistence

import Auditable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.kro.dearmoment.image.domain.Image

@Entity
@Table(name = "images")
class ImageEntity(
    @Id
    @Column(name = "image_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column
    val userId: Long,
    @Column
    val url: String,
    @Column
    val fileName: String,
) : Auditable() {
    fun toDomain(): Image {
        return Image(
            imageId = id,
            userId = userId,
            url = url,
            fileName = fileName,
        )
    }

    companion object {
        fun from(domain: Image) =
            ImageEntity(
                id = domain.imageId,
                userId = domain.userId,
                url = domain.url,
                fileName = domain.fileName,
            )
    }
}
