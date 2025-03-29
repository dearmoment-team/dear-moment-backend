package kr.kro.dearmoment.like.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import java.util.UUID

class LikeTest : DescribeSpec({
    describe("스튜디오 좋아요는") {
        context("생성 시 유저 ID가 빈 값이면") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> { CreateProductLike(userId = UUID.fromString(""), productId = 1) }
            }
        }

        context("생성 시 대상 ID가 양수가 아니면") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> { CreateProductLike(userId = UUID.randomUUID(), productId = 0) }
                shouldThrow<IllegalArgumentException> { CreateProductLike(userId = UUID.randomUUID(), productId = -1) }
            }
        }
    }

    describe("상품 옵션 좋아요는") {
        context("생성 시 유저 ID가 양수가 아니면") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> { CreateProductOptionLike(userId = UUID.fromString(""), productOptionId = 1) }
            }
        }

        context("생성 시 대상 ID가 양수가 아니면") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> { CreateProductOptionLike(userId = UUID.randomUUID(), productOptionId = 0) }
                shouldThrow<IllegalArgumentException> { CreateProductOptionLike(userId = UUID.randomUUID(), productOptionId = -1) }
            }
        }
    }
})
