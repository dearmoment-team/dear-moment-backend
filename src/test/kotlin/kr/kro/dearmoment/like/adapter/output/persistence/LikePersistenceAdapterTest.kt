package kr.kro.dearmoment.like.adapter.output.persistence

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.productOptionEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.like.domain.CreateProductOptionLike
import kr.kro.dearmoment.like.domain.CreateStudioLike
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductOptionRepository
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductRepository
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository

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

        describe("saveStudioLike() 는") {
            val userId = 1L
            val savedStudio = studioRepository.save(studioEntityFixture())
            val studioLike = CreateStudioLike(userId = userId, studioId = savedStudio.id)
            val studioLikeId = adapter.saveStudioLike(studioLike)

            context("저장하려는 스튜디오 좋아요 도메인이 전달되면") {
                it("DB에 저장한다.") {
                    studioLikeId shouldBeGreaterThan 0
                }
            }
        }

        describe("[예외] saveStudioLike() 는") {
            val userId = 1L
            val savedStudio = studioRepository.save(studioEntityFixture())
            val studioLike = CreateStudioLike(userId = userId, studioId = savedStudio.id)

            adapter.saveStudioLike(studioLike)
            context("동일 유저가 동일 스튜디오에 대한 좋아요를 저장하려고 시도하면 ") {
                it("예외를 발생 시킨다.") {
                    shouldThrow<CustomException> {
                        adapter.saveStudioLike(studioLike)
                    }.apply {
                        errorCode shouldBe ErrorCode.LIKE_DUPLICATED
                    }
                }
            }
        }

        describe("saveProductOptionLike()는") {
            val userId = 1L
            val savedStudio = studioRepository.save(studioEntityFixture(userId))
            val savedProduct = productRepository.save(productEntityFixture(userId, savedStudio))
            val savedProductOption = productOptionRepository.save(productOptionEntityFixture(savedProduct))
            val productOptionLike =
                CreateProductOptionLike(userId = userId, productOptionId = savedProductOption.optionId)
            val productOptionLikeId = adapter.saveProductOptionLike(productOptionLike)

            context("저장하려는 상품 좋아요 도메인이 전달되면") {
                it("DB에 저장한다.") {
                    productOptionLikeId shouldBeGreaterThan 0
                }
            }
        }

        describe("[예외] saveProductOptionLike()는") {
            val userId = 1L
            val savedStudio = studioRepository.save(studioEntityFixture(userId))
            val savedProduct = productRepository.save(productEntityFixture(userId, savedStudio))
            val savedProductOption = productOptionRepository.save(productOptionEntityFixture(savedProduct))
            val productOptionLike =
                CreateProductOptionLike(userId = userId, productOptionId = savedProductOption.optionId)
            adapter.saveProductOptionLike(productOptionLike)

            context("동일 유저가 동일 상품 옵션에 대한 좋아요를 저장하려고 시도하면") {
                it("예외를 발생 시킨다.") {
                    shouldThrow<CustomException> {
                        adapter.saveProductOptionLike(productOptionLike)
                    }.apply {
                        errorCode shouldBe ErrorCode.LIKE_DUPLICATED
                    }
                }
            }
        }

        describe("deleteXXXLike()는") {
            context("like ID가 전달되면") {
                it("like ID에 해당하는 데이터를 db에서 삭제한다. ") {
                    shouldNotThrow<Throwable> { adapter.deleteStudioLike(1L) }
                    shouldNotThrow<Throwable> { adapter.deleteProductOptionLike(1L) }
                }
            }
        }
    })
