package kr.kro.dearmoment.like.application.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kr.kro.dearmoment.common.fixture.productOptionFixture
import kr.kro.dearmoment.like.application.command.SaveLikeCommand
import kr.kro.dearmoment.like.application.command.UnlikeProductCommand
import kr.kro.dearmoment.like.application.command.UnlikeProductOptionCommand
import kr.kro.dearmoment.like.application.port.output.DeleteLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort
import kr.kro.dearmoment.like.domain.CreateProductLike
import kr.kro.dearmoment.like.domain.CreateProductOptionLike
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionReadOnlyRepository
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import java.util.UUID

class LikeServiceTest : DescribeSpec({

    val saveLikePort = mockk<SaveLikePort>()
    val deleteLikePort = mockk<DeleteLikePort>()
    val productPersistencePort = mockk<ProductPersistencePort>()
    val optionReadRepository = mockk<ProductOptionReadOnlyRepository>()
    val likeCommandService =
        LikeCommandService(
            saveLikePort,
            deleteLikePort,
            productPersistencePort,
            optionReadRepository,
        )

    describe("productLike()는") {
        context("유효한 command를 전달 받으면") {
            val command = SaveLikeCommand(userId = UUID.randomUUID(), targetId = 2L)
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
            }
        }
    }

    describe("productOptionLike()는") {
        context("유효한 command를 전달 받으면") {
            val command = SaveLikeCommand(userId = UUID.randomUUID(), targetId = 2L)
            val like =
                CreateProductOptionLike(
                    id = 1L,
                    userId = command.userId,
                    productOptionId = command.targetId,
                )

            every { saveLikePort.saveProductOptionLike(any()) } returns like.id
            every { optionReadRepository.findById(command.targetId) } returns productOptionFixture()
            every { productPersistencePort.increaseOptionLikeCount(any()) } just Runs
            it("likeEntity를 저장하고 like를 반환한다.") {
                val response = likeCommandService.productOptionsLike(command)
                response.likeId shouldBe like.id
            }
        }
    }

    describe("productUnlike()는") {
        context("좋아요 ID를 전달 받으면") {
            val command =
                UnlikeProductCommand(
                    productId = 1L,
                    likeId = 1L,
                    userId = UUID.randomUUID(),
                )

            every { deleteLikePort.deleteProductLike(command.userId, command.likeId) } just Runs
            every { productPersistencePort.decreaseLikeCount(command.productId) } just Runs
            it("like를 삭제한다.") {
                shouldNotThrow<Throwable> { likeCommandService.productUnlike(command) }
            }
        }
    }

    describe("productOptionUnlike()는") {
        context("좋아요 ID를 전달 받으면") {
            val command =
                UnlikeProductOptionCommand(
                    likeId = 1L,
                    productOptionId = 1L,
                    userId = UUID.randomUUID(),
                )

            every { deleteLikePort.deleteProductOptionLike(command.userId, command.likeId) } just Runs
            every { optionReadRepository.findById(command.productOptionId) } returns productOptionFixture()
            every { productPersistencePort.decreaseOptionLikeCount(any()) } just Runs
            it("like를 삭제한다.") {
                shouldNotThrow<Throwable> { likeCommandService.productOptionUnlike(command) }
            }
        }
    }
})
