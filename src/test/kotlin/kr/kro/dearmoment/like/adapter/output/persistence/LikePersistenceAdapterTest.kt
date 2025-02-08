package kr.kro.dearmoment.like.adapter.output.persistence

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.like.domain.Like
import kr.kro.dearmoment.like.domain.LikeType

@RepositoryTest
class LikePersistenceAdapterTest(
    private val likeRepository: JpaLikeRepository,
) : DescribeSpec({
        val adapter = LikePersistenceAdapter(likeRepository)

        describe("LikePersistenceAdapter.save()는") {
            context("like 도메인이 전달되면") {
                val like = Like(userId = 1L, targetId = 2L, type = LikeType.AUTHOR)
                it("도메인을 엔티티로 변환하여 DB에 저장한다.") {
                    val result = adapter.save(like)
                    result.shouldNotBeNull()
                }
            }
        }

        describe("LikePersistenceAdapter.delete()는") {
            context("like ID가 전달되면") {
                val like = Like(userId = 1L, targetId = 2L, type = LikeType.AUTHOR)
                val savedId = adapter.save(like)
                it("like ID에 해당하는 데이터를 db에서 삭제한다. ") {
                    shouldNotThrow<Throwable> { adapter.delete(savedId) }
                }
            }
        }
    })
