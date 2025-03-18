package kr.kro.dearmoment.like.adapter.output.persistence

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.productOptionEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.like.domain.CreateProductOptionLike
import kr.kro.dearmoment.like.domain.CreateStudioLike
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductOptionRepository
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductRepository
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@RepositoryTest
class LikePersistenceAdapterTest(
    private val studioLikeRepository: StudioLikeJpaRepository,
    private val productLikeRepository: ProductOptionLikeJpaRepository,
    private val studioRepository: StudioJpaRepository,
    private val productOptionRepository: JpaProductOptionRepository,
    private val productRepository: JpaProductRepository,
) : DescribeSpec({

        val adapter =
            LikePersistenceAdapter(
                studioLikeRepository,
                productLikeRepository,
                studioRepository,
                productOptionRepository,
            )

        describe("LikePersistenceAdapter는") {
            val userId = 1L
            val savedStudio = studioRepository.save(studioEntityFixture(userId))
            val savedProduct = productRepository.save(productEntityFixture(userId, savedStudio))
            val savedProductOption = productOptionRepository.save(productOptionEntityFixture(savedProduct))

            val studioLike = CreateStudioLike(userId = userId, studioId = savedStudio.id)
            val studioLikeId = adapter.saveStudioLike(studioLike)

            val productOptionLike =
                CreateProductOptionLike(userId = userId, productOptionId = savedProductOption.optionId)
            val productOptionLikeId = adapter.saveProductOptionLike(productOptionLike)

            context("저장하려는 스튜디오 좋아요 도메인이 전달되면") {
                it("DB에 저장한다.") {
                    studioLikeId shouldBeGreaterThan 0
                }
            }

            context("저장하려는 상품 좋아요 도메인이 전달되면") {
                it("DB에 저장한다.") {
                    productOptionLikeId shouldBeGreaterThan 0
                }
            }

            context("유저 스튜디오 좋아요를 조회하기 위해 user ID와 페이징 정보를 전달하면") {
                val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
                it("DB에서 유저의 스튜디오 좋아요를 반환한다.") {
                    val result = adapter.findUserStudioLikes(userId, pageable)

                    result.totalElements shouldBe 1L
                    result.content.size shouldBe 1
                    result.totalPages shouldBe 1
                    result.number shouldBe pageable.pageNumber
                    result.size shouldBe pageable.pageSize
                }
            }

            context("유저 스튜디오 좋아요를 조회하기 위해 user ID와 페이징 정보를 전달하면") {
                val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
                it("DB에서 유저의 스튜디오 좋아요를 반환한다.") {
                    val result = adapter.findUserStudioLikes(userId, pageable)

                    result.totalElements shouldBe 1L
                    result.content.size shouldBe 1
                    result.totalPages shouldBe 1
                    result.number shouldBe pageable.pageNumber
                    result.size shouldBe pageable.pageSize
                }
            }

            context("user ID와 studio ID가 전달되면") {
                it("좋아요가 존재하는지 알 수있다") {
                    val isLike = adapter.existStudioLike(studioLike.userId, savedStudio.id)
                    isLike shouldBe true
                }
            }

            context("user ID와 product ID가 전달되면") {
                it("좋아요가 존재하는지 알 수있다") {
                    val isLike = adapter.existProductOptionLike(productOptionLike.userId, savedProductOption.optionId)
                    isLike shouldBe true
                }
            }

            context("like ID가 전달되면") {
                it("like ID에 해당하는 데이터를 db에서 삭제한다. ") {
                    shouldNotThrow<Throwable> { adapter.deleteStudioLike(studioLikeId) }
                    shouldNotThrow<Throwable> { adapter.deleteProductOptionLike(productOptionLikeId) }
                }
            }
        }
    })
