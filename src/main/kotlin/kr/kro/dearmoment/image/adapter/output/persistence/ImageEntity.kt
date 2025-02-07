package kr.kro.dearmoment.image.adapter.output.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity

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
    val fileName: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    var product: ProductEntity? = null,
) : Auditable() {
    companion object {
        fun from(domain: Image) =
            ImageEntity(
                userId = domain.userId,
                fileName = domain.fileName,
            )

        fun toDomain(entity: ImageEntity): Image {
            return Image(
                imageId = entity.id,
                userId = entity.userId,
                fileName = entity.fileName,
            )
        }
    }
}
