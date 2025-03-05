package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.shouldHaveMessage
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@Import(ProductPersistenceAdapter::class)
@ActiveProfiles("test")
class ProductPersistenceAdapterTest(
    @Autowired private val productPersistencePort: ProductPersistencePort,
    @Autowired private val jpaProductRepository: JpaProductRepository,
    @Autowired private val jpaProductOptionRepository: JpaProductOptionRepository,
) : DescribeSpec({

        describe("ProductPersistenceAdapter 테스트") {

            beforeEach {
                jpaProductOptionRepository.deleteAllInBatch()
                jpaProductRepository.deleteAllInBatch()
            }

            afterEach {
                jpaProductOptionRepository.deleteAllInBatch()
                jpaProductRepository.deleteAllInBatch()
            }

            context("상품 생성 유효성 검증") {
                it("패키지 상품은 협력업체 정보가 반드시 필요해야 함") {
                    val exception =
                        shouldThrow<IllegalArgumentException> {
                            Product(
                                userId = 1L,
                                productType = ProductType.WEDDING_SNAP,
                                shootingPlace = ShootingPlace.JEJU,
                                title = "프로 패키지",
                                mainImage =
                                    kr.kro.dearmoment.image.domain.Image(
                                        userId = 1L,
                                        fileName = "main.jpg",
                                        url = "http://example.com/main.jpg",
                                    ),
                                subImages = emptyList(),
                                additionalImages = emptyList(),
                            )
                        }
                    exception shouldHaveMessage "서브 이미지는 정확히 4장 등록해야 합니다."
                }

                it("모든 상품은 필수 이미지(대표 이미지와 서브 이미지 4장)가 필요함") {
                    val exception =
                        shouldThrow<IllegalArgumentException> {
                            Product(
                                userId = 1L,
                                productType = ProductType.WEDDING_SNAP,
                                shootingPlace = ShootingPlace.JEJU,
                                title = "스튜디오 촬영",
                                mainImage =
                                    kr.kro.dearmoment.image.domain.Image(
                                        userId = 1L,
                                        fileName = "main.jpg",
                                        url = "http://example.com/main.jpg",
                                    ),
                                subImages = emptyList(),
                                additionalImages = emptyList(),
                            )
                        }
                    exception shouldHaveMessage "서브 이미지는 정확히 4장 등록해야 합니다."
                }
            }

            context("상품 저장 시") {
                it("상품의 필수 정보가 정상 저장되어야 함") {
                    // Given
                    val sampleProduct =
                        createSampleProduct(
                            userId = 1L,
                            title = "[프리미엄] 웨딩 촬영 패키지",
                            productType = ProductType.WEDDING_SNAP,
                            shootingPlace = ShootingPlace.JEJU,
                            mainImage =
                                kr.kro.dearmoment.image.domain.Image(
                                    userId = 1L,
                                    fileName = "main.jpg",
                                    url = "http://example.com/main.jpg",
                                ),
                            subImages =
                                List(4) {
                                    kr.kro.dearmoment.image.domain.Image(
                                        userId = 1L,
                                        fileName = "sub${it + 1}.jpg",
                                        url = "http://example.com/sub${it + 1}.jpg",
                                    )
                                },
                            additionalImages =
                                listOf(
                                    kr.kro.dearmoment.image.domain.Image(
                                        userId = 1L,
                                        fileName = "add1.jpg",
                                        url = "http://example.com/add1.jpg",
                                    ),
                                ),
                            detailedInfo = "상세 정보",
                            contactInfo = "contact@example.com",
                        )

                    // When
                    val savedProduct = productPersistencePort.save(sampleProduct)
                    jpaProductRepository.flush()

                    // Then
                    with(savedProduct) {
                        productId shouldNotBe 0L
                        title shouldBe "[프리미엄] 웨딩 촬영 패키지"
                        productType shouldBe ProductType.WEDDING_SNAP
                        shootingPlace shouldBe ShootingPlace.JEJU
                        mainImage.fileName shouldBe "main.jpg"
                        subImages shouldHaveSize 4
                        additionalImages.size shouldBe 1
                        detailedInfo shouldBe "상세 정보"
                        contactInfo shouldBe "contact@example.com"
                    }
                }
            }

            context("상품 검색 기능") {
                lateinit var testProducts: List<Product>

                beforeEach {
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
                        ).map { productPersistencePort.save(it) }
                    jpaProductRepository.flush()
                }

                it("제목으로 검색 시 해당 상품만 반환되어야 함") {
                    // testProducts를 이용하여 기대값 생성
                    val expectedTitles = testProducts.filter { it.title.contains("스냅") }.map { it.title }
                    val results =
                        productPersistencePort.searchByCriteria(
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
            )
    }
}
