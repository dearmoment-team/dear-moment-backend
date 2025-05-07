package kr.kro.dearmoment.like.adapter.output.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.like.domain.ProductLike
import java.util.UUID

class ProductLikeEntityTest : DescribeSpec({
    describe("ProductLikeEntity") {
        val userId = UUID.randomUUID()
        val product = productEntityFixture(userId, studioEntityFixture(userId), 1L)

        context("생성 테스트") {
            it("ProductLikeEntity가 정상적으로 생성된다") {
                val productLikeEntity =
                    ProductLikeEntity(
                        userId = userId,
                        product = product,
                    )

                productLikeEntity.userId shouldBe userId
                productLikeEntity.product shouldBe product
            }
        }

        context("toDomain() 변환 테스트") {
            it("ProductLikeEntity가 ProductLike 도메인 객체로 변환된다") {
                val productLikeEntity =
                    ProductLikeEntity(
                        userId = userId,
                        product = product,
                    )

                val domain = productLikeEntity.toDomain()

                domain.id shouldBe productLikeEntity.id
                domain.userId shouldBe productLikeEntity.userId
            }
        }

        context("from() 팩토리 메서드 테스트") {
            it("Like 도메인 객체와 ProductEntity를 사용해 ProductLikeEntity가 생성된다") {
                val like = ProductLike(userId = userId, product = product.toDomain())

                val productLikeEntity = ProductLikeEntity.from(like, product)

                productLikeEntity.userId shouldBe like.userId
                productLikeEntity.product shouldBe product
            }
        }
    }
})
