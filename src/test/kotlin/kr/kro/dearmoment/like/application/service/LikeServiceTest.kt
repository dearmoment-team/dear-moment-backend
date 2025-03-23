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
import kr.kro.dearmoment.like.application.command.UnlikeProductCommand
import kr.kro.dearmoment.like.application.port.output.DeleteLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort
import kr.kro.dearmoment.like.domain.CreateProductLike
import kr.kro.dearmoment.like.domain.CreateProductOptionLike
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort

class LikeServiceTest : DescribeSpec({

    val saveLikePort = mockk<SaveLikePort>()
    val deleteLikePort = mockk<DeleteLikePort>()
    val productPersistencePort = mockk<ProductPersistencePort>()
    val likeCommandService = LikeCommandService(saveLikePort, deleteLikePort, productPersistencePort)

    describe("productLike()는") {
        context("유효한 command를 전달 받으면") {
            val command = SaveLikeCommand(userId = 1L, targetId = 2L)
            val like =
                CreateProductLike(
                    id = 1L,
                    userId = command.userId,
                    productId = command.targetId,
                )
            every { saveLikePort.saveProductLike(any()) } returns like.id
            every { productPersistencePort.increaseLikeCount(command.targetId) } just Runs
            it("likeEntity를 저장하고 like를 반환한다.") {
                val response = likeCommandService.productLike(command)
                response.likeId shouldBe like.id
                verify(exactly = 1) { saveLikePort.saveProductLike(any()) }
                verify(exactly = 1) { productPersistencePort.increaseLikeCount(command.targetId) }
            }
        }
    }

    describe("productOptionLike()는") {
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
                val response = likeCommandService.productOptionsLike(command)
                response.likeId shouldBe like.id
                verify(exactly = 1) { saveLikePort.saveProductOptionLike(any()) }
            }
        }
    }

    describe("productUnlike()는") {
        context("좋아요 ID를 전달 받으면") {
            val command =
                UnlikeProductCommand(
                    productId = 1L,
                    likeId = 1L,
                )

            every { deleteLikePort.deleteProductLike(command.likeId) } just Runs
            every { productPersistencePort.decreaseLikeCount(command.productId) } just Runs
            it("like를 삭제한다.") {
                shouldNotThrow<Throwable> { likeCommandService.productUnlike(command) }
                verify(exactly = 1) { deleteLikePort.deleteProductLike(command.likeId) }
                verify(exactly = 1) { productPersistencePort.decreaseLikeCount(command.productId) }
            }
        }
    }

    describe("productOptionUnlike()는") {
        context("좋아요 ID를 전달 받으면") {
            val likeId = 1L

            every { deleteLikePort.deleteProductOptionLike(likeId) } just Runs
            it("like를 삭제한다.") {
                shouldNotThrow<Throwable> { likeCommandService.productOptionUnlike(likeId) }
                verify(exactly = 1) { deleteLikePort.deleteProductOptionLike(likeId) }
            }
        }
    }
})
