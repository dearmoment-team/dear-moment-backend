package kr.kro.dearmoment.like.adapter.output.persistence

import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
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
class LikeReadOnlyPersistenceAdapterTest(
    private val productOptionLikeJpaRepository: ProductOptionLikeJpaRepository,
    private val studioLikeJpaRepository: StudioLikeJpaRepository,
    private val studioRepository: StudioJpaRepository,
    private val productOptionRepository: JpaProductOptionRepository,
    private val productRepository: JpaProductRepository,
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

        val likePersistenceAdapter =
            LikePersistenceAdapter(
                studioLikeJpaRepository,
                productOptionLikeJpaRepository,
                studioRepository,
                productOptionRepository,
            )

        afterTest {
            studioLikeJpaRepository.deleteAll()
            productOptionLikeJpaRepository.deleteAll()
            productOptionRepository.deleteAll()
            productRepository.deleteAll()
            studioRepository.deleteAll()
        }

        describe("findUserProductOptionLikes()는") {
            val userId1 = 1L
            val savedStudio = studioRepository.save(studioEntityFixture())
            val savedProduct = productRepository.save(productEntityFixture(studioEntity = savedStudio))
            val savedProductOption = productOptionRepository.save(productOptionEntityFixture(savedProduct))

            val productOptionLike =
                CreateProductOptionLike(userId = userId1, productOptionId = savedProductOption.optionId)

            likePersistenceAdapter.saveProductOptionLike(productOptionLike)

            context("유저 상품 옵션 좋아요를 조회하기 위해 user ID와 페이징 정보를 전달하면") {
                val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
                it("DB에서 유저의 상품 옵션 좋아요를 반환한다.") {
                    val result = adapter.findUserProductOptionLikes(1L, pageable)

                    result.totalElements shouldBe 1.toLong()
                    result.content.size shouldBe 1
                    result.totalPages shouldBe (1 / pageable.pageSize) + if (1 % pageable.pageSize > 0) 1 else 0
                    result.number shouldBe pageable.pageNumber
                    result.size shouldBe pageable.pageSize
                }
            }
        }

        describe("findUserStudioLikes()는") {
            val userId1 = 1L
            val savedStudio = studioRepository.save(studioEntityFixture())
            val studioLike = CreateStudioLike(userId = userId1, studioId = savedStudio.id)

            likePersistenceAdapter.saveStudioLike(studioLike)

            context("유저 스튜디오 좋아요를 조회하기 위해 user ID와 페이징 정보를 전달하면") {
                val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
                it("DB에서 유저의 스튜디오 좋아요를 반환한다.") {
                    val result = adapter.findUserStudioLikes(1L, pageable)

                    result.totalElements shouldBe 1.toLong()
                    result.content.size shouldBe 1
                    result.totalPages shouldBe (1 / pageable.pageSize) + if (1 % pageable.pageSize > 0) 1 else 0
                    result.number shouldBe pageable.pageNumber
                    result.size shouldBe pageable.pageSize
                }
            }
        }

        describe("existStudioLike()는") {
            val userId1 = 1L
            val savedStudio = studioRepository.save(studioEntityFixture())
            val studioLike = CreateStudioLike(userId = userId1, studioId = savedStudio.id)

            likePersistenceAdapter.saveStudioLike(studioLike)

            context("user ID와 studio ID가 전달되면") {
                it("스튜디오 좋아요가 존재하는지 알 수있다") {
                    val isLike = adapter.existStudioLike(userId1, savedStudio.id)
                    isLike shouldBe true
                }
            }
        }

        describe("existProductOptionLike()는") {
            val userId1 = 1L
            val savedStudio = studioRepository.save(studioEntityFixture())
            val savedProduct = productRepository.save(productEntityFixture(studioEntity = savedStudio))
            val savedProductOption = productOptionRepository.save(productOptionEntityFixture(savedProduct))
            val productOptionLike =
                CreateProductOptionLike(userId = userId1, productOptionId = savedProductOption.optionId)

            likePersistenceAdapter.saveProductOptionLike(productOptionLike)

            context("user ID와 product ID가 전달되면") {
                it("상품 옵션 좋아요가 존재하는지 알 수있다") {
                    val isLike = adapter.existProductOptionLike(userId1, savedProductOption.optionId)
                    isLike shouldBe true
                }
            }
        }
    })
