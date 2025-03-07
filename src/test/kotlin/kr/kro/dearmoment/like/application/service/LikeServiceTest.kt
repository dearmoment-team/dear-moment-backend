package kr.kro.dearmoment.like.application.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.like.application.command.SaveLikeCommand
import kr.kro.dearmoment.like.application.port.output.DeleteLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort
import kr.kro.dearmoment.like.domain.CreateProductOptionLike
import kr.kro.dearmoment.like.domain.CreateStudioLike

class LikeServiceTest : DescribeSpec({

    val saveLikePort = mockk<SaveLikePort>()
    val deleteLikePort = mockk<DeleteLikePort>()
    val likeService = LikeService(saveLikePort, deleteLikePort)

    describe("studioLike()는") {
        context("유효한 command를 전달 받으면") {
            val command = SaveLikeCommand(userId = 1L, targetId = 2L)
            val like =
                CreateStudioLike(
                    id = 1L,
                    userId = command.userId,
                    studioId = command.targetId,
                )

            every { saveLikePort.saveStudioLike(any()) } returns like.id
            it("likeEntity를 저장하고 like를 반환한다.") {
                val response = likeService.studioLike(command)
                response.likeId shouldBe like.id
                verify(exactly = 1) { saveLikePort.saveStudioLike(any()) }
            }
        }
    }

    describe("productLike()는") {
        context("유효한 command를 전달 받으면") {
            val command = SaveLikeCommand(userId = 1L, targetId = 2L)
            val like =
                CreateProductOptionLike(
                    id = 1L,
                    userId = command.userId,
                    productOptionId = command.targetId,
                )

            every { saveLikePort.saveProductOptionLike(any()) } returns like.id
            it("likeEntity를 저장하고 like를 반환한다.") {
                val response = likeService.productOptionsLike(command)
                response.likeId shouldBe like.id
                verify(exactly = 1) { saveLikePort.saveProductOptionLike(any()) }
            }
        }
    }

    describe("studioUnlike()는") {
        context("좋아요 ID를 전달 받으면") {
            val likeId = 1L

            every { deleteLikePort.deleteStudioLike(likeId) } just Runs
            it("like를 삭제한다.") {
                shouldNotThrow<Throwable> { likeService.studioUnlike(likeId) }
                verify(exactly = 1) { deleteLikePort.deleteStudioLike(likeId) }
            }
        }
    }

    describe("productUnlike()는") {
        context("좋아요 ID를 전달 받으면") {
            val likeId = 1L

            every { deleteLikePort.deleteProductOptionLike(likeId) } just Runs
            it("like를 삭제한다.") {
                shouldNotThrow<Throwable> { likeService.productOptionUnlike(likeId) }
                verify(exactly = 1) { deleteLikePort.deleteProductOptionLike(likeId) }
            }
        }
    }
})
