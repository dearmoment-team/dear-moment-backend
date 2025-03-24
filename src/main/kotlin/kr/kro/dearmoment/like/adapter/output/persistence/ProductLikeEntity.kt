package kr.kro.dearmoment.like.adapter.output.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.like.domain.Like
import kr.kro.dearmoment.like.domain.ProductLike
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity

@Entity
@Table(
    name = "product_likes",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "product_id"])],
)
class ProductLikeEntity(
    @Id
    @Column(name = "like_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(nullable = false)
    val userId: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: ProductEntity? = null,
) : Auditable() {
    fun toDomain(): ProductLike {
        val notNullProduct = requireNotNull(product)
        return ProductLike(
            id = id,
            userId = userId,
            product = notNullProduct.toDomain(),
        )
    }

    companion object {
        fun from(
            like: Like,
            product: ProductEntity,
        ) = ProductLikeEntity(
            userId = like.userId,
            product = product,
        )
    }
}
