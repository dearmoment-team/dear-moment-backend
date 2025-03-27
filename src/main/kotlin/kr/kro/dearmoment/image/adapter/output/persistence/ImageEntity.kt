package kr.kro.dearmoment.image.adapter.output.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.image.domain.Image
import java.util.*

@Entity
@Table(name = "images")
class ImageEntity(
    @Id
    @Column(name = "image_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column
    val userId: UUID,
    @Column
    val fileName: String,
    url: String,
    parId: String,
) : Auditable() {
    @Column
    var url: String = url
        protected set

    @Column
    var parId: String = parId
        protected set

    fun modifyUrlInfo(
        modifiedUrl: String,
        modifiedParId: String,
    ) {
        url = modifiedUrl
        parId = modifiedParId
    }

    fun toDomain() =
        Image(
            imageId = id,
            userId = userId,
            url = url,
            fileName = fileName,
            parId = parId,
        )

    companion object {
        fun from(domain: Image) =
            ImageEntity(
                id = domain.imageId,
                userId = domain.userId,
                url = domain.url,
                fileName = domain.fileName,
                parId = domain.parId,
            )
    }
}
