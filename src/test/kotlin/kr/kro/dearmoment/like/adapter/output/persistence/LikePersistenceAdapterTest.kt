package kr.kro.dearmoment.like.adapter.output.persistence

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.like.domain.Like
import kr.kro.dearmoment.like.domain.LikeType

@RepositoryTest
class LikePersistenceAdapterTest(
    private val likeRepository: JpaLikeRepository,
) : DescribeSpec({
        val adapter = LikePersistenceAdapter(likeRepository)

        describe("LikePersistenceAdapter는") {
            val like = Like(userId = 1L, targetId = 2L, type = LikeType.AUTHOR)
            val likeId = adapter.save(like)

            context("like 도메인이 전달되면") {
                it("도메인을 엔티티로 변환하여 DB에 저장한다.") {
                    likeId.shouldNotBeNull()
                }
            }

            context("user ID와 target ID가 전달되면") {
                it("좋아요가 존재하는지 알수있다") {
                    adapter.existLike(like.userId, like.targetId, like.type.value) shouldBe true
                }
            }

            context("like ID가 전달되면") {
                it("like ID에 해당하는 데이터를 db에서 삭제한다. ") {
                    shouldNotThrow<Throwable> { adapter.delete(likeId) }
                }
            }

            context("user ID가 전달되면") {
                val userId = 2L
                val likes =
                    listOf(
                        Like(userId = userId, targetId = 1L, type = LikeType.AUTHOR),
                        Like(userId = userId, targetId = 2L, type = LikeType.AUTHOR),
                        Like(userId = userId, targetId = 3L, type = LikeType.AUTHOR),
                        Like(userId = userId, targetId = 1L, type = LikeType.PRODUCT),
                        Like(userId = userId, targetId = 2L, type = LikeType.PRODUCT),
                    )

                likes.forEach { adapter.save(it) }
                it("해당 유저의 좋아요를 모두 조회한다.") {
                    adapter.loadLikes(userId).size shouldBe 5
                }
            }
        }
    })
