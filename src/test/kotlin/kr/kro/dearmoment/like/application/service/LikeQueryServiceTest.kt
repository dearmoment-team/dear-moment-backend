package kr.kro.dearmoment.like.application.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.fixture.productLikeFixture
import kr.kro.dearmoment.common.fixture.productOptionLikeFixture
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.like.application.query.ExistLikeQuery
import kr.kro.dearmoment.like.application.query.GetUserProductLikeQuery
import kr.kro.dearmoment.like.application.query.GetUserProductOptionLikeQuery
import kr.kro.dearmoment.like.domain.ProductLike
import kr.kro.dearmoment.like.domain.ProductOptionLike
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

class LikeQueryServiceTest : DescribeSpec({
    val getLikePort = mockk<GetLikePort>()
    val service = LikeQueryService(getLikePort)

    describe("isProductLike()는") {
        context("ExistLikeQuery가 전달되면") {
            val query = ExistLikeQuery(userId = 1L, targetId = 1L)
            every { getLikePort.existProductLike(query.userId, query.targetId) } returns true

            it("좋아요가 존재하는지 알 수 있다.") {
                shouldNotThrow<Throwable> { service.isProductLike(query) }
                verify(exactly = 1) { getLikePort.existProductLike(query.userId, query.targetId) }
            }
        }
    }

    describe("isProductOptionLike()는") {
        context("ExistLikeQuery가 전달되면") {
            val query = ExistLikeQuery(userId = 1L, targetId = 1L)
            every { getLikePort.existProductOptionLike(query.userId, query.targetId) } returns true

            it("좋아요가 존재하는지 알 수 있다.") {
                shouldNotThrow<Throwable> { service.isProductOptionLike(query) }
                verify(exactly = 1) { getLikePort.existProductOptionLike(query.userId, query.targetId) }
            }
        }
    }

    describe("getUserProductLikes()") {
        context("userId가 전달되면") {
            val userId = 1L
            val likes = List(3) { productLikeFixture(userId) }
            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
            val page: Page<ProductLike> = PageImpl(likes, pageable, likes.size.toLong())

            every { getLikePort.findUserProductLikes(userId, pageable) } returns page
            it("유저가 좋아요한 상품 정보를 모두 조회한다.") {
                val result = service.getUserProductLikes(GetUserProductLikeQuery(userId, pageable))

                result.totalElements shouldBe likes.size.toLong()
                result.content.size shouldBe likes.size
                result.totalPages shouldBe 1
                result.page shouldBe 0
                result.size shouldBe 10

                verify(exactly = 1) { getLikePort.findUserProductLikes(userId, pageable) }
            }
        }
    }

    describe("getUserProductOptionLikes()") {
        context("userId가 전달되면") {
            val userId = 1L
            val likes = List(3) { productOptionLikeFixture(userId) }
            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
            val page: Page<ProductOptionLike> = PageImpl(likes, pageable, likes.size.toLong())

            every { getLikePort.findUserProductOptionLikes(userId, pageable) } returns page
            it("유저가 좋아요한 상품 옵션 정보를 모두 조회한다.") {
                val result = service.getUserProductOptionLikes(GetUserProductOptionLikeQuery(userId, pageable))

                result.totalElements shouldBe likes.size.toLong()
                result.content.size shouldBe likes.size
                result.totalPages shouldBe 1
                result.page shouldBe 0
                result.size shouldBe 10

                verify(exactly = 1) { getLikePort.findUserProductOptionLikes(userId, pageable) }
            }
        }
    }
})
