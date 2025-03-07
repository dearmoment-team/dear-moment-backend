package kr.kro.dearmoment.like.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec

class LikeTest : DescribeSpec({
    describe("스튜디오 좋아요는") {
        context("생성 시 유저 ID가 양수가 아니면") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> { CreateStudioLike(userId = 0, studioId = 1) }
                shouldThrow<IllegalArgumentException> { CreateStudioLike(userId = -1, studioId = 1) }
            }
        }

        context("생성 시 대상 ID가 양수가 아니면") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> { CreateStudioLike(userId = 1, studioId = 0) }
                shouldThrow<IllegalArgumentException> { CreateStudioLike(userId = 1, studioId = -1) }
            }
        }
    }

    describe("상품 옵션 좋아요는") {
        context("생성 시 유저 ID가 양수가 아니면") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> { CreateProductOptionLike(userId = 0, productOptionId = 1) }
                shouldThrow<IllegalArgumentException> { CreateProductOptionLike(userId = -1, productOptionId = 1) }
            }
        }

        context("생성 시 대상 ID가 양수가 아니면") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> { CreateProductOptionLike(userId = 1, productOptionId = 0) }
                shouldThrow<IllegalArgumentException> { CreateProductOptionLike(userId = 1, productOptionId = -1) }
            }
        }
    }
})
