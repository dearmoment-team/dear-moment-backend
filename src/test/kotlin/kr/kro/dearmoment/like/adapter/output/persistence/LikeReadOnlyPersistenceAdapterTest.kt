package kr.kro.dearmoment.like.adapter.output.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.ReadOnlyRepositoryTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@ReadOnlyRepositoryTest
class LikeReadOnlyPersistenceAdapterTest(
    private val productOptionLikeJpaRepository: ProductOptionLikeJpaRepository,
    private val studioLikeJpaRepository: StudioLikeJpaRepository,
) : DescribeSpec({

        val adapter = LikeReadOnlyPersistenceAdapter(productOptionLikeJpaRepository, studioLikeJpaRepository)

        describe("findUserProductOptionLikes()는") {
            val likes: List<ProductOptionLikeEntity> = emptyList()
            context("유저 스튜디오 좋아요를 조회하기 위해 user ID와 페이징 정보를 전달하면") {
                val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
                it("DB에서 유저의 스튜디오 좋아요를 반환한다.") {
                    val result = adapter.findUserProductOptionLikes(1L, pageable)

                    result.totalElements shouldBe likes.size.toLong()
                    result.content.size shouldBe likes.size
                    result.totalPages shouldBe (likes.size / pageable.pageSize) + if (likes.size % pageable.pageSize > 0) 1 else 0
                    result.number shouldBe pageable.pageNumber
                    result.size shouldBe pageable.pageSize
                }
            }
        }

        describe("findUserStudioLikes()는") {
            val likes: List<StudioLikeEntity> = emptyList()
            context("유저 스튜디오 좋아요를 조회하기 위해 user ID와 페이징 정보를 전달하면") {
                val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
                it("DB에서 유저의 스튜디오 좋아요를 반환한다.") {
                    val result = adapter.findUserStudioLikes(1L, pageable)

                    result.totalElements shouldBe likes.size.toLong()
                    result.content.size shouldBe likes.size
                    result.totalPages shouldBe (likes.size / pageable.pageSize) + if (likes.size % pageable.pageSize > 0) 1 else 0
                    result.number shouldBe pageable.pageNumber
                    result.size shouldBe pageable.pageSize
                }
            }
        }

//    describe("existStudioLike()는") {
//        context("user ID와 studio ID가 전달되면") {
//            it("좋아요가 존재하는지 알 수있다") {
//                val isLike = adapter.existStudioLike(1L, 1L)
//                isLike shouldBe false
//            }
//        }
//    }
//
//    describe("existProductOptionLike()는") {
//        context("user ID와 product ID가 전달되면") {
//            it("좋아요가 존재하는지 알 수있다") {
//                val isLike = adapter.existProductOptionLike(1L, 1L)
//                isLike shouldBe false
//            }
//        }
//    }
    })
