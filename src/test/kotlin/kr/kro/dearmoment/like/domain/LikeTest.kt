package kr.kro.dearmoment.like.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec

class LikeTest : DescribeSpec({
    describe("좋아요는") {
        val likeType = LikeType.ARTIST

        context("생성 시 유저 ID가 양수가 아니면") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> { Like(userId = 0, targetId = 1, type = likeType) }
                shouldThrow<IllegalArgumentException> { Like(userId = -1, targetId = 1, type = likeType) }
            }
        }

        context("생성 시 대상 ID가 양수가 아니면") {
            it("예외를 발생시킨다.") {
                shouldThrow<IllegalArgumentException> { Like(userId = 1, targetId = 0, type = likeType) }
                shouldThrow<IllegalArgumentException> { Like(userId = 1, targetId = -1, type = likeType) }
            }
        }
    }
})
