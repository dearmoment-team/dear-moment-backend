package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldNotBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.common.fixture.studioFixture
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import kr.kro.dearmoment.studio.domain.Studio
import java.time.LocalDateTime

@RepositoryTest
class ProductPersistenceAdapterTest(
    private val jpaProductRepository: JpaProductRepository,
    private val jpaProductOptionRepository: JpaProductOptionRepository,
    private val studioRepository: StudioJpaRepository,
) : DescribeSpec({
        val adapter = ProductPersistenceAdapter(studioRepository, jpaProductRepository, jpaProductOptionRepository)

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
                    jpaProductRepository.increaseLikeCount(savedProduct.productId!!) shouldNotBe 0
                }
            }

            context("decreaseLikeCount()는 ") {
                it("상품의 좋아요 개수를 1개 증가 시킨다.") {
                    jpaProductRepository.decreaseLikeCount(savedProduct.productId!!) shouldNotBe 0
                }
            }
        }
        describe("ProductPersistenceAdapter 상품 문의 증감 테스트") {
            val savedStudio = studioRepository.save(studioEntityFixture())
            val savedProduct = jpaProductRepository.save(productEntityFixture(studioEntity = savedStudio))

            context("increaseInquiryCount()는 ") {
                it("상품의 좋아요 개수를 1개 증가 시킨다.") {
                    jpaProductRepository.increaseInquiryCount(savedProduct.productId!!) shouldNotBe 0
                }
            }

            context("decreaseInquiryCount()는 ") {
                it("상품의 좋아요 개수를 1개 증가 시킨다.") {
                    jpaProductRepository.decreaseInquiryCount(savedProduct.productId!!) shouldNotBe 0
                }
            }
        }

        describe("ProductPersistenceAdapter 상품 검색 테스트") {
            context("상품 검색 기능") {
                lateinit var testProducts: List<Product>

                beforeEach {
                    val savedStudio = studioRepository.save(studioEntityFixture(userId = 1L))
                    testProducts =
                        listOf(
                            createSampleProduct(
                                userId = 1L,
                                title = "스냅 사진 기본 패키지",
                                productType = ProductType.WEDDING_SNAP,
                                shootingPlace = ShootingPlace.JEJU,
                            ),
                            createSampleProduct(
                                userId = 1L,
                                title = "개인 스튜디오 대여",
                                productType = ProductType.WEDDING_SNAP,
                                shootingPlace = ShootingPlace.JEJU,
                            ),
                            createSampleProduct(
                                userId = 1L,
                                title = "아기 사진 전문 촬영",
                                productType = ProductType.WEDDING_SNAP,
                                shootingPlace = ShootingPlace.JEJU,
                            ),
                        ).map { adapter.save(it, savedStudio.id) }
                }

                it("제목으로 검색 시 해당 상품만 반환되어야 함") {
                    // testProducts를 이용하여 기대값 생성
                    val expectedTitles = testProducts.filter { it.title.contains("스냅") }.map { it.title }
                    val results =
                        adapter.searchByCriteria(
                            title = "스냅",
                            productType = null,
                            shootingPlace = null,
                            sortBy = "created-desc",
                        )
                    results.map { it.title } shouldContainExactlyInAnyOrder expectedTitles
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
