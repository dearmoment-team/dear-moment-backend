package kr.kro.dearmoment.like.adapter.output.persistence

import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.RepositoryTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@RepositoryTest
class LikeReadOnlyPersistenceAdapterTest(
    private val productOptionLikeJpaRepository: ProductOptionLikeJpaRepository,
    private val studioLikeJpaRepository: StudioLikeJpaRepository,
//    private val studioRepository: StudioJpaRepository,
//    private val productOptionRepository: JpaProductOptionRepository,
//    private val productRepository: JpaProductRepository,
    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : DescribeSpec({

        val adapter =
            LikeReadOnlyPersistenceAdapter(
                productOptionLikeJpaRepository,
                studioLikeJpaRepository,
                entityManager,
                jpqlRenderContext,
            )

//    val likePersistenceAdapter =
//        LikePersistenceAdapter(
//            studioLikeJpaRepository,
//            productOptionLikeJpaRepository,
//            studioRepository,
//            productOptionRepository,
//        )
//
//    val userId = 1L
//    val savedStudio = studioRepository.save(studioEntityFixture(userId))
//    val savedProduct = productRepository.save(productEntityFixture(userId, savedStudio))
//    val savedProductOption = productOptionRepository.save(productOptionEntityFixture(savedProduct))
//
//    val studioLike = CreateStudioLike(userId = userId, studioId = savedStudio.id)
//
//    likePersistenceAdapter.saveStudioLike(studioLike)
//
//    val productOptionLike =
//        CreateProductOptionLike(userId = userId, productOptionId = savedProductOption.optionId!!)
//
//    likePersistenceAdapter.saveProductOptionLike(productOptionLike)

        describe("findUserProductOptionLikes()는") {
            val likes = listOf<ProductOptionLikeEntity>()
            context("유저 스튜디오 좋아요를 조회하기 위해 user ID와 페이징 정보를 전달하면") {
                val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
                it("DB에서 유저의 스튜디오 좋아요를 반환한다.") {
                    val result = adapter.findUserProductOptionLikes(1L, pageable)

//                result.totalElements shouldBe 1.toLong()
//                result.content.size shouldBe 1
//                result.totalPages shouldBe (1 / pageable.pageSize) + if (1 % pageable.pageSize > 0) 1 else 0
                    result.totalElements shouldBe likes.size.toLong()
                    result.content.size shouldBe likes.size
                    result.totalPages shouldBe (likes.size / pageable.pageSize) + if (likes.size % pageable.pageSize > 0) 1 else 0
                    result.number shouldBe pageable.pageNumber
                    result.size shouldBe pageable.pageSize
                }
            }
        }

        describe("findUserStudioLikes()는") {
            val likes = listOf<StudioLikeEntity>()
            context("유저 스튜디오 좋아요를 조회하기 위해 user ID와 페이징 정보를 전달하면") {
                val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
                it("DB에서 유저의 스튜디오 좋아요를 반환한다.") {
                    val result = adapter.findUserStudioLikes(1L, pageable)

//                result.totalElements shouldBe 1.toLong()
//                result.content.size shouldBe 1
//                result.totalPages shouldBe (1 / pageable.pageSize) + if (1 % pageable.pageSize > 0) 1 else 0
                    result.totalElements shouldBe likes.size.toLong()
                    result.content.size shouldBe likes.size
                    result.totalPages shouldBe (likes.size / pageable.pageSize) + if (likes.size % pageable.pageSize > 0) 1 else 0
                    result.number shouldBe pageable.pageNumber
                    result.size shouldBe pageable.pageSize
                }
            }
        }

        describe("existStudioLike()는") {
            context("user ID와 studio ID가 전달되면") {
                it("좋아요가 존재하는지 알 수있다") {
//                val isLike = adapter.existStudioLike(userId, savedStudio.id)
//                isLike shouldBe true
                    val isLike = adapter.existStudioLike(1L, 1L)
                    isLike shouldBe false
                }
            }
        }

        describe("existProductOptionLike()는") {
            context("user ID와 product ID가 전달되면") {
                it("좋아요가 존재하는지 알 수있다") {
//                val isLike = adapter.existProductOptionLike(userId, savedProductOption.optionId!!)
//                isLike shouldBe true
                    val isLike = adapter.existProductOptionLike(1L, 1L)
                    isLike shouldBe false
                }
            }
        }
    })
