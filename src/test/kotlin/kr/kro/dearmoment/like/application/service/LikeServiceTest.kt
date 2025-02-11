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
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort
import kr.kro.dearmoment.like.application.query.ExistLikeQuery
import kr.kro.dearmoment.like.application.query.GetLikesQuery
import kr.kro.dearmoment.like.domain.Like
import kr.kro.dearmoment.like.domain.LikeType

class LikeServiceTest : DescribeSpec({

    val saveLikePort = mockk<SaveLikePort>()
    val deleteLikePort = mockk<DeleteLikePort>()
    val getLikePort = mockk<GetLikePort>()
    val likeService = LikeService(saveLikePort, deleteLikePort, getLikePort)

    describe("like()는") {
        context("유효한 command를 전달 받으면") {
            val command = LikeCommand(userId = 1L, targetId = 2L, type = LikeType.AUTHOR.value)

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

    describe("getLikes()는") {
        context("GetLikesQuery가 전달되면") {
            val query =
                GetLikesQuery(
                    userId = 1L,
                    likeType = LikeType.AUTHOR.value,
                )

            val likes =
                listOf(
                    Like(userId = query.userId, targetId = 1L, type = LikeType.AUTHOR),
                    Like(userId = query.userId, targetId = 2L, type = LikeType.AUTHOR),
                    Like(userId = query.userId, targetId = 3L, type = LikeType.AUTHOR),
                    Like(userId = query.userId, targetId = 1L, type = LikeType.PRODUCT),
                    Like(userId = query.userId, targetId = 2L, type = LikeType.PRODUCT),
                )

            every { getLikePort.loadLikes(query.userId) } returns likes
            it("쿼리의 좋아요 타입에 해당하는 좋아요를 모두 조회한다.") {
                likeService.getLikes(query).size shouldBe 3
            }
        }
    }

    describe("isLike()는") {
        context("ExistLikeQuery가 전달되면") {
            val query = ExistLikeQuery(userId = 1L, targetId = 1L, type = LikeType.AUTHOR.value)
            every { getLikePort.existLike(query.userId, query.targetId, query.type) } returns true

            it("좋아요가 존재하는지 알 수 있다.") {
                shouldNotThrow<Throwable> { likeService.isLike(query) }
                verify(exactly = 1) { getLikePort.existLike(query.userId, query.targetId, query.type) }
            }
        }
    }
})
