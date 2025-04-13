package kr.kro.dearmoment.like.application.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.fixture.productLikeFixture
import kr.kro.dearmoment.common.fixture.productOptionLikeFixture
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.like.application.query.ExistLikeQuery
import kr.kro.dearmoment.like.application.query.FilterUserLikesQuery
import kr.kro.dearmoment.like.application.query.GetUserProductLikeQuery
import kr.kro.dearmoment.like.application.query.GetUserProductOptionLikeQuery
import kr.kro.dearmoment.like.domain.ProductLike
import kr.kro.dearmoment.like.domain.ProductOptionLike
import kr.kro.dearmoment.like.domain.SortCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.UUID

class LikeQueryServiceTest : DescribeSpec({
    val getLikePort = mockk<GetLikePort>()
    val service = LikeQueryService(getLikePort)

    describe("isProductLike()는") {
        context("ExistLikeQuery가 전달되면") {
            val query = ExistLikeQuery(userId = UUID.randomUUID(), targetId = 1L)
            every { getLikePort.existProductLike(query.userId, query.targetId) } returns true

            it("좋아요가 존재하는지 알 수 있다.") {
                shouldNotThrow<Throwable> { service.isProductLike(query) }
                verify(exactly = 1) { getLikePort.existProductLike(query.userId, query.targetId) }
            }
        }
    }

    describe("isProductOptionLike()는") {
        context("ExistLikeQuery가 전달되면") {
            val query = ExistLikeQuery(userId = UUID.randomUUID(), targetId = 1L)
            every { getLikePort.existProductOptionLike(query.userId, query.targetId) } returns true

            it("좋아요가 존재하는지 알 수 있다.") {
                shouldNotThrow<Throwable> { service.isProductOptionLike(query) }
                verify(exactly = 1) { getLikePort.existProductOptionLike(query.userId, query.targetId) }
            }
        }
    }

    describe("getUserProductLikes()") {
        context("userId가 전달되면") {
            val userId = UUID.randomUUID()
            val likes = List(3) { productLikeFixture(userId) }
            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
            val page: Page<ProductLike> = PageImpl(likes, pageable, likes.size.toLong())

            every { getLikePort.findUserProductLikes(userId, pageable) } returns page
            it("유저가 좋아요한 상품 정보를 모두 조회한다.") {
                val result = service.getUserProductLikes(GetUserProductLikeQuery(userId, pageable))

                result.content.size shouldBe likes.size
                result.page shouldBe 0
                result.size shouldBe 10

                verify(exactly = 1) { getLikePort.findUserProductLikes(userId, pageable) }
            }
        }
    }

    describe("filterUserProductsLikes()") {
        context("userId와 필터 조건이 주어지면") {
            val userId = UUID.randomUUID()
            val likeList = List(10) { productLikeFixture(userId) }

            val query =
                FilterUserLikesQuery(
                    minPrice = likeList.minOf { it.product.options.minOf { it.discountPrice } },
                    maxPrice = likeList.maxOf { it.product.options.maxOf { it.discountPrice } },
                    partnerShopCategories = emptySet(),
                    availableSeasons = emptySet(),
                    cameraTypes = emptySet(),
                    retouchStyles = emptySet(),
                    sortBy = SortCriteria.PRICE_LOW
                )

            every { getLikePort.findUserProductLikes(userId) } returns likeList

            it("필터링 및 정렬된 상품 좋아요 목록을 반환한다") {
                val result = service.filterUserProductsLikes(userId, query)
                result shouldHaveSize likeList.size
                verify(exactly = 1) { getLikePort.findUserProductLikes(userId) }
            }
        }
    }

    describe("getUserProductOptionLikes()") {
        context("userId가 전달되면") {
            val userId = UUID.randomUUID()
            val likes = List(3) { productOptionLikeFixture(userId) }
            val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
            val page: Page<ProductOptionLike> = PageImpl(likes, pageable, likes.size.toLong())

            every { getLikePort.findUserProductOptionLikes(userId, pageable) } returns page
            it("유저가 좋아요한 상품 옵션 정보를 모두 조회한다.") {
                val result = service.getUserProductOptionLikes(GetUserProductOptionLikeQuery(userId, pageable))

                result.content.size shouldBe likes.size
                result.page shouldBe 0
                result.size shouldBe 10

                verify(exactly = 1) { getLikePort.findUserProductOptionLikes(userId, pageable) }
            }
        }
    }

    describe("filterUserProductsOptionsLikes()") {
        context("userId와 필터 조건이 주어지면") {
            val userId = UUID.randomUUID()
            val optionLikeList = List(10) { productOptionLikeFixture(userId) }

            val query =
                FilterUserLikesQuery(
                    minPrice = optionLikeList.minOf { it.product.options.minOf { it.discountPrice } },
                    maxPrice = optionLikeList.maxOf { it.product.options.maxOf { it.discountPrice } },
                    partnerShopCategories = emptySet(),
                    availableSeasons = emptySet(),
                    cameraTypes = emptySet(),
                    retouchStyles = emptySet(),
                    sortBy = SortCriteria.PRICE_LOW
                )

            every { getLikePort.findUserProductOptionLikes(userId) } returns optionLikeList

            it("필터링 및 가격 낮은 순 정렬된 상품 옵션 좋아요 목록을 반환한다") {
                val result = service.filterUserProductsOptionsLikes(userId, query)
                result shouldHaveSize optionLikeList.size
                verify(exactly = 1) { getLikePort.findUserProductOptionLikes(userId) }
            }
        }
    }
})
