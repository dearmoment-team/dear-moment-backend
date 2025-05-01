package kr.kro.dearmoment.like.adapter.output.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.common.fixture.imageEntityFixture
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.productOptionEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.like.domain.ProductOptionLike
import java.util.UUID

class ProductOptionLikeEntityTest : DescribeSpec({
    describe("ProductOptionLikeEntity") {
        val userId = UUID.randomUUID()
        val studio = studioEntityFixture(userId, imageEntityFixture(userId))
        val product = productEntityFixture(userId, studio, 1L)
        val productOptionEntity = productOptionEntityFixture(product, 1L)

        val like =
            ProductOptionLike(
                id = 1L,
                userId = UUID.randomUUID(),
                studioName = studio.name,
                productOptionId = productOptionEntity.optionId,
                product = product.toDomain(),
            )

        it("Like와 ProductOptionEntity에서 유효한 ProductOptionLikeEntity가 생성되어야 한다") {
            val productOptionLikeEntity = ProductOptionLikeEntity.from(like, productOptionEntity)

            productOptionLikeEntity.userId shouldBe like.userId
            productOptionLikeEntity.option shouldBe productOptionEntity
        }

        it("도메인 객체(ProductOptionLike)로 변환되어야 한다") {
            val productOptionLikeEntity = ProductOptionLikeEntity.from(like, productOptionEntity)
            val result = productOptionLikeEntity.toDomain()

            result.id shouldBe productOptionLikeEntity.id
            result.userId shouldBe productOptionLikeEntity.userId
            result.productOptionId shouldBe productOptionEntity.optionId
        }

        it("option이 null일 경우 예외가 발생해야 한다") {
            val invalidProductOptionLikeEntity =
                ProductOptionLikeEntity(
                    id = 1L,
                    userId = UUID.randomUUID(),
                    option = null,
                )

            shouldThrow<IllegalStateException> {
                invalidProductOptionLikeEntity.toDomain()
            }
        }
    }
})
