package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldNotBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.common.fixture.studioFixture
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.option.ProductOption
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import kr.kro.dearmoment.studio.domain.Studio
import java.time.LocalDateTime

@RepositoryTest
class ProductPersistenceAdapterTest(
    private val jpaProductRepository: JpaProductRepository,
    private val jpaProductOptionRepository: JpaProductOptionRepository,
    private val studioRepository: StudioJpaRepository,
) : DescribeSpec({
        afterEach {
            jpaProductOptionRepository.deleteAllInBatch()
            jpaProductRepository.deleteAllInBatch()
            studioRepository.deleteAll()
        }

        describe("ProductPersistenceAdapter 상품 저장 테스트") {
            context("상품 저장 시") {
                val savedStudio = studioRepository.save(studioEntityFixture())
                val savedProduct = jpaProductRepository.save(productEntityFixture(studioEntity = savedStudio))
                it("상품의 필수 정보가 정상 저장되어야 함") {
                    savedProduct.productId shouldNotBe 0L
                }
            }
        }

        describe("ProductPersistenceAdapter 상품 좋아요 증감 테스트") {
            val savedStudio = studioRepository.save(studioEntityFixture())
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
            val savedStudio = studioRepository.save(studioEntityFixture())
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
    }) {
    companion object {
        fun createSampleProduct(
            userId: Long = 1L,
            title: String = "기본 상품",
            productType: ProductType = ProductType.WEDDING_SNAP,
            shootingPlace: ShootingPlace = ShootingPlace.JEJU,
            mainImage: kr.kro.dearmoment.image.domain.Image =
                kr.kro.dearmoment.image.domain.Image(
                    userId = userId,
                    fileName = "main.jpg",
                    url = "http://example.com/main.jpg",
                ),
            subImages: List<kr.kro.dearmoment.image.domain.Image> =
                List(4) {
                    kr.kro.dearmoment.image.domain.Image(
                        userId = userId,
                        fileName = "sub${it + 1}.jpg",
                        url = "http://example.com/sub${it + 1}.jpg",
                    )
                },
            additionalImages: List<kr.kro.dearmoment.image.domain.Image> = emptyList(),
            detailedInfo: String = "",
            contactInfo: String = "contact@example.com",
            options: List<ProductOption> = emptyList(),
            studio: Studio = studioFixture(),
        ): Product =
            Product(
                productId = 0L,
                userId = userId,
                productType = productType,
                shootingPlace = shootingPlace,
                title = title,
                description = "",
                availableSeasons = emptySet(),
                cameraTypes = emptySet(),
                retouchStyles = emptySet(),
                mainImage = mainImage,
                subImages = subImages,
                additionalImages = additionalImages,
                detailedInfo = detailedInfo,
                contactInfo = contactInfo,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                options = options,
                studio = studio,
            )
    }
}
