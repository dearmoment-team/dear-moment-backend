package kr.kro.dearmoment.like.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec

class LikeTypeTest : DescribeSpec({
    describe("좋아요 타입은") {
        context("유효한 타입이 아니라면") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> { LikeType.from("Invalid Type") }
            }
        }
    }
})
