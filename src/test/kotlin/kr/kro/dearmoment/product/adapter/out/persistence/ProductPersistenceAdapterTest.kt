package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldNotBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.fixture.imageEntityFixture
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.image.adapter.output.persistence.JpaImageRepository
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import java.util.UUID

@RepositoryTest
class ProductPersistenceAdapterTest(
    private val jpaProductRepository: JpaProductRepository,
    private val jpaProductOptionRepository: JpaProductOptionRepository,
    private val studioRepository: StudioJpaRepository,
    private val imageRepository: JpaImageRepository,
) : DescribeSpec({

        afterEach {
            jpaProductOptionRepository.deleteAllInBatch()
            jpaProductRepository.deleteAllInBatch()
            studioRepository.deleteAll()
            imageRepository.deleteAll()
        }

        describe("ProductPersistenceAdapter 상품 저장 테스트") {
            context("상품 저장 시") {
                val userId = UUID.randomUUID()
                val image = imageRepository.save(imageEntityFixture(userId))
                val savedStudio = studioRepository.save(studioEntityFixture(userId, image))
                val savedProduct = jpaProductRepository.save(productEntityFixture(studioEntity = savedStudio))
                it("상품의 필수 정보가 정상 저장되어야 함") {
                    savedProduct.productId shouldNotBe 0L
                }
            }
        }

        describe("ProductPersistenceAdapter 상품 좋아요 증감 테스트") {
            val userId = UUID.randomUUID()
            val image = imageRepository.save(imageEntityFixture(userId))
            val savedStudio = studioRepository.save(studioEntityFixture(userId, image))
            val savedProduct = jpaProductRepository.save(productEntityFixture(studioEntity = savedStudio))

            context("increaseLikeCount()는 ") {
                it("상품의 좋아요 개수를 1개 증가 시킨다.") {
                    shouldNotThrow<Throwable> { jpaProductRepository.increaseLikeCount(savedProduct.productId!!) }
                }
            }

            context("decreaseLikeCount()는 ") {
                it("상품의 좋아요 개수를 1개 감소 시킨다.") {
                    shouldNotThrow<Throwable> { jpaProductRepository.decreaseLikeCount(savedProduct.productId!!) }
                }
            }
        }

        describe("ProductPersistenceAdapter 상품 문의 증감 테스트") {
            val userId = UUID.randomUUID()
            val image = imageRepository.save(imageEntityFixture(userId))
            val savedStudio = studioRepository.save(studioEntityFixture(userId, image))
            val savedProduct = jpaProductRepository.save(productEntityFixture(studioEntity = savedStudio))

            context("increaseInquiryCount()는 ") {
                it("상품의 문의 개수를 1개 증가 시킨다.") {
                    shouldNotThrow<Throwable> { jpaProductRepository.increaseInquiryCount(savedProduct.productId!!) }
                }
            }

            context("decreaseInquiryCount()는 ") {
                it("상품의 문의 개수를 1개 감소 시킨다.") {
                    shouldNotThrow<Throwable> { jpaProductRepository.decreaseInquiryCount(savedProduct.productId!!) }
                }
            }
        }

        describe("ProductPersistenceAdapter 상품 옵션 좋아요 증감 테스트") {
            val userId = UUID.randomUUID()
            val image = imageRepository.save(imageEntityFixture(userId))
            val savedStudio = studioRepository.save(studioEntityFixture(userId, image))
            val savedProduct = jpaProductRepository.save(productEntityFixture(studioEntity = savedStudio))

            context("increaseOptionLikeCount()는 ") {
                it("상품 옵션 좋아요 개수를 1개 증가 시킨다.") {
                    shouldNotThrow<Throwable> { jpaProductRepository.increaseOptionLikeCount(savedProduct.productId!!) }
                }
            }

            context("decreaseOptionLikeCount()는 ") {
                it("상품 옵션 좋아요 개수를 1개 감소 시킨다.") {
                    shouldNotThrow<Throwable> { jpaProductRepository.decreaseOptionLikeCount(savedProduct.productId!!) }
                }
            }
        }
    })
