package kr.kro.dearmoment.like.adapter.output.persistence

import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.productOptionEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.like.domain.CreateProductLike
import kr.kro.dearmoment.like.domain.CreateProductOptionLike
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductOptionRepository
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductRepository
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.UUID

@RepositoryTest
class LikeReadOnlyPersistenceAdapterTest(
    private val productOptionLikeJpaRepository: ProductOptionLikeJpaRepository,
    private val productLikeJpaRepository: ProductLikeJpaRepository,
    private val studioRepository: StudioJpaRepository,
    private val productOptionRepository: JpaProductOptionRepository,
    private val productRepository: JpaProductRepository,
    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : DescribeSpec({
        val adapter =
            LikeReadOnlyPersistenceAdapter(
                productOptionLikeJpaRepository,
                productLikeJpaRepository,
                entityManager,
                jpqlRenderContext,
            )

        val likePersistenceAdapter =
            LikePersistenceAdapter(
                productLikeJpaRepository,
                productOptionLikeJpaRepository,
                productRepository,
                productOptionRepository,
            )

        afterTest {
            productLikeJpaRepository.deleteAll()
            productOptionLikeJpaRepository.deleteAll()
            productOptionRepository.deleteAll()
            productRepository.deleteAll()
        }

        describe("findUserProductOptionLikes()는") {
            val userId1 = UUID.randomUUID()
            val savedStudio = studioRepository.save(studioEntityFixture())
            val savedProduct = productRepository.save(productEntityFixture(studioEntity = savedStudio))
            val savedProductOption = productOptionRepository.save(productOptionEntityFixture(savedProduct))

            val productOptionLike =
                CreateProductOptionLike(userId = userId1, productOptionId = savedProductOption.optionId)

            likePersistenceAdapter.saveProductOptionLike(productOptionLike)

            context("유저 상품 옵션 좋아요를 조회하기 위해 user ID와 페이징 정보를 전달하면") {
                val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
                it("DB에서 유저의 상품 옵션 좋아요를 반환한다.") {
                    val result = adapter.findUserProductOptionLikes(userId1, pageable)

                    result.totalElements shouldBe 1.toLong()
                    result.content.size shouldBe 1
                    result.totalPages shouldBe (1 / pageable.pageSize) + if (1 % pageable.pageSize > 0) 1 else 0
                    result.number shouldBe pageable.pageNumber
                    result.size shouldBe pageable.pageSize
                }
            }
        }

        describe("findUserProductLikes()는") {
            val userId1 = UUID.randomUUID()
            val savedStudio = studioRepository.save(studioEntityFixture())
            val savedProduct = productRepository.save(productEntityFixture(studioEntity = savedStudio))
            val studioLike = CreateProductLike(userId = userId1, productId = savedProduct.productId!!)
            likePersistenceAdapter.saveProductLike(studioLike)

            context("유저 스튜디오 좋아요를 조회하기 위해 user ID와 페이징 정보를 전달하면") {
                val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdDate"))
                it("DB에서 유저의 스튜디오 좋아요를 반환한다.") {
                    val result = adapter.findUserProductLikes(userId1, pageable)

                    result.totalElements shouldBe 1.toLong()
                    result.content.size shouldBe 1
                    result.totalPages shouldBe (1 / pageable.pageSize) + if (1 % pageable.pageSize > 0) 1 else 0
                    result.number shouldBe pageable.pageNumber
                    result.size shouldBe pageable.pageSize
                }
            }
        }

        describe("existProductLike()는") {
            val userId1 = UUID.randomUUID()
            val savedStudio = studioRepository.save(studioEntityFixture())
            val savedProduct = productRepository.save(productEntityFixture(studioEntity = savedStudio))
            val studioLike = CreateProductLike(userId = userId1, productId = savedProduct.productId!!)

            likePersistenceAdapter.saveProductLike(studioLike)

            context("user ID와 studio ID가 전달되면") {
                it("스튜디오 좋아요가 존재하는지 알 수있다") {
                    val isLike = adapter.existProductLike(userId1, savedStudio.id)
                    isLike shouldBe true
                }
            }
        }

        describe("existProductOptionLike()는") {
            val userId1 = UUID.randomUUID()
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

        describe("findOptionLikesByUserIdAndOptionIds()는") {
            val userId = UUID.randomUUID()
            val savedStudio = studioRepository.save(studioEntityFixture())
            val savedProduct = productRepository.save(productEntityFixture(studioEntity = savedStudio))
            val savedProductOption = productOptionRepository.save(productOptionEntityFixture(savedProduct))
            val productOptionLike =
                CreateProductOptionLike(userId = userId, productOptionId = savedProductOption.optionId)

            likePersistenceAdapter.saveProductOptionLike(productOptionLike)

            val optionIds = listOf(savedProductOption.optionId)

            context("user ID와 product ID들이 전달되면") {
                it("유저의 상품 좋아요를 조회한다.") {
                    val likes = adapter.findOptionLikesByUserIdAndOptionIds(userId, optionIds)
                    likes.size shouldBe listOf(productOptionLike).size
                }
            }
        }

        describe("findUserProductLikesWithoutPage()는") {
            val userId = UUID.randomUUID()
            val savedStudio = studioRepository.save(studioEntityFixture())
            val savedProduct = productRepository.save(productEntityFixture(studioEntity = savedStudio))
            val productLike = CreateProductLike(userId = userId, productId = savedProduct.productId!!)

            likePersistenceAdapter.saveProductLike(productLike)

            val productIds = listOf(savedProduct.productId!!)

            context("user ID와 product ID들이 전달되면") {
                it("유저의 상품 좋아요를 조회한다.") {
                    val likes = adapter.findProductLikesByUserIdAndProductIds(userId, productIds)
                    likes.size shouldBe listOf(productLike).size
                }
            }
        }
    })
