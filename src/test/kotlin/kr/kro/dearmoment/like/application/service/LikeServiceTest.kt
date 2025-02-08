package kr.kro.dearmoment.like.application.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.like.application.command.LikeCommand
import kr.kro.dearmoment.like.application.port.output.DeleteLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort

class LikeServiceTest : DescribeSpec({

    val saveLikePort = mockk<SaveLikePort>()
    val deleteLikePort = mockk<DeleteLikePort>()
    val likeService = LikeService(saveLikePort, deleteLikePort)

    describe("like()는") {
        context("유효한 command를 전달 받으면") {
            val command = LikeCommand(userId = 1L, targetId = 2L, type = "AUTHOR")

            every { saveLikePort.save(any()) } returns 1L
            it("like를 저장하고 ID를 반환한다.") {
                val response = likeService.like(command)
                response.likeId shouldBe 1L
                verify(exactly = 1) { saveLikePort.save(any()) }
            }
        }
    }

    describe("unlike()는") {
        context("좋아요 ID를 전달 받으면") {
            val likeId = 1L

            every { deleteLikePort.delete(likeId) } just Runs
            it("like를 삭제한다.") {
                shouldNotThrow<Throwable> { likeService.unlike(likeId) }
                verify(exactly = 1) { deleteLikePort.delete(likeId) }
            }
        }
    }
})
