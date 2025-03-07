package kr.kro.dearmoment.like.application.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.fixture.productOptionLikeFixture
import kr.kro.dearmoment.common.fixture.studioLikeFixture
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.like.application.query.ExistLikeQuery

class LikeQueryServiceTest : DescribeSpec({
    val getLikePort = mockk<GetLikePort>()
    val service = LikeQueryService(getLikePort)

    describe("isStudioLike()는") {
        context("ExistLikeQuery가 전달되면") {
            val query = ExistLikeQuery(userId = 1L, targetId = 1L)
            every { getLikePort.existStudioLike(query.userId, query.targetId) } returns true

            it("좋아요가 존재하는지 알 수 있다.") {
                shouldNotThrow<Throwable> { service.isStudioLike(query) }
                verify(exactly = 1) { getLikePort.existStudioLike(query.userId, query.targetId) }
            }
        }
    }

    describe("isProductLike()는") {
        context("ExistLikeQuery가 전달되면") {
            val query = ExistLikeQuery(userId = 1L, targetId = 1L)
            every { getLikePort.existProductOptionLike(query.userId, query.targetId) } returns true

            it("좋아요가 존재하는지 알 수 있다.") {
                shouldNotThrow<Throwable> { service.isProductOptionLike(query) }
                verify(exactly = 1) { getLikePort.existProductOptionLike(query.userId, query.targetId) }
            }
        }
    }

    describe("getUserStudioLikes()") {
        context("userId가 전달되면") {
            val userId = 1L
            val expected = List(3) { studioLikeFixture(userId) }

            every { getLikePort.findUserStudioLikes(userId) } returns expected
            it("유저가 좋아요한 스튜디오 정보를 모두 조회한다.") {
                val result = service.getUserStudioLikes(userId)
                result.size shouldBe expected.size
            }
        }
    }

    describe("getUserProductOptionLikes()") {
        context("userId가 전달되면") {
            val userId = 1L
            val expected = List(3) { productOptionLikeFixture(userId) }

            every { getLikePort.findUserProductLikes(userId) } returns expected
            it("유저가 좋아요한 상품 옵션 정보를 모두 조회한다.") {
                val result = service.getUserProductOptionLikes(userId)
                result.size shouldBe expected.size
            }
        }
    }
})
