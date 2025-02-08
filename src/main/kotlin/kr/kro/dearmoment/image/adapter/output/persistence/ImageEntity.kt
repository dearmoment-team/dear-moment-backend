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
    val url: String,
    @Column
    val fileName: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    var product: ProductEntity? = null,
) : Auditable() {
    // 인스턴스 메서드로 toDomain()을 구현하여 Image 객체를 반환하도록 함
    fun toDomain(): Image {
        return Image(
            imageId = id,
            userId = userId,
            url = url,
            fileName = fileName,
        )
    }

    companion object {
        // from() 메서드의 시그니처를 수정하여 ImageEntity를 반환하도록 함
        fun from(domain: Image): ImageEntity =
            ImageEntity(
                userId = domain.userId,
                url = domain.url,
                fileName = domain.fileName,
            )
    }
}
