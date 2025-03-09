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
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.like.domain.Like
import kr.kro.dearmoment.like.domain.ProductOptionLike
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionEntity

@Entity
@Table(name = "product_option_likes")
class ProductOptionLikeEntity(
    @Id
    @Column(name = "like_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(nullable = false)
    val userId: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    val option: ProductOptionEntity? = null,
) : Auditable() {
    fun toDomain(): ProductOptionLike {
        check(option != null) { "상품 옵션은 널이 될 수 없습니다." }
        val product = requireNotNull(option.product)
        val studio = requireNotNull(product.studio)

        return ProductOptionLike(
            id = id,
            userId = userId,
            productOptionId = requireNotNull(option.optionId),
            studioName = studio.name,
            product = requireNotNull(product.toDomain()),
        )
    }

    companion object {
        fun from(
            like: Like,
            option: ProductOptionEntity,
        ) = ProductOptionLikeEntity(
            userId = like.userId,
            option = option,
        )
    }
}
