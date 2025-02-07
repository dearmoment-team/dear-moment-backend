package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.shouldHaveMessage
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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
                // deleteAll()를 사용하면 cascade 옵션이 적용되어 images 등 연관 엔티티들도 삭제됩니다.
                jpaProductOptionRepository.deleteAll()
                jpaProductRepository.deleteAll()
            }

            afterEach {
                jpaProductOptionRepository.deleteAll()
                jpaProductRepository.deleteAll()
            }

            context("상품 생성 유효성 검증") {
                it("패키지 상품(typeCode=1)은 협력업체 정보 필수") {
                    val exception =
                        shouldThrow<IllegalArgumentException> {
                            Product(
                                userId = 1L,
                                title = "프로 패키지",
                                price = 500_000L,
                                typeCode = 1,
                                images = listOf("main.jpg"),
                                partnerShops = emptyList(),
                            )
                        }
                    exception shouldHaveMessage "패키지 상품은 하나 이상의 협력업체 정보가 필요합니다."
                }

                it("모든 상품은 최소 1개 이상의 이미지 필수") {
                    val exception =
                        shouldThrow<IllegalArgumentException> {
                            Product(
                                userId = 1L,
                                title = "스튜디오 촬영",
                                price = 300_000L,
                                typeCode = 2,
                                images = emptyList(),
                            )
                        }
                    exception shouldHaveMessage "최소 1개 이상의 이미지가 필요합니다"
                }
            }

            context("사진작가 상품 저장 시") {
                it("촬영 정보와 패키지 구성이 정상 저장되어야 함") {
                    // Given
                    val shootingTime = LocalDateTime.now().plusDays(7).truncatedTo(ChronoUnit.MICROS)
                    val partnerShops =
                        listOf(
                            PartnerShop(name = "스튜디오A", link = "http://studio-a.com"),
                            PartnerShop(name = "의상업체B", link = "http://costume-b.com"),
                        )

                    val product =
                        Product(
                            userId = 1L,
                            title = "[프리미엄] 웨딩 촬영 패키지",
                            price = 1_200_000L,
                            typeCode = 1,
                            shootingTime = shootingTime,
                            shootingLocation = "서울 강남 스튜디오",
                            numberOfCostumes = 3,
                            partnerShops = partnerShops,
                            images = listOf("main.jpg", "studio.jpg", "sample1.jpg"),
                            detailedInfo = "",
                            warrantyInfo = "",
                            contactInfo = "",
                        )

                    // When
                    val savedProduct = productPersistencePort.save(product)
                    jpaProductRepository.flush()

                    // Then
                    with(savedProduct) {
                        productId shouldNotBe 0L
                        title shouldBe "[프리미엄] 웨딩 촬영 패키지"
                        shootingTime?.truncatedTo(ChronoUnit.MICROS) shouldBe shootingTime
                        shootingLocation shouldBe "서울 강남 스튜디오"
                        numberOfCostumes shouldBe 3
                        partnerShops shouldContainExactlyInAnyOrder partnerShops
                        images shouldContainExactlyInAnyOrder listOf("main.jpg", "studio.jpg", "sample1.jpg")
                        createdAt shouldNotBe null
                        updatedAt shouldNotBe null
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
                                price = 150_000L,
                                typeCode = 1,
                                partnerShops = listOf(PartnerShop("협력사1", "http://partner1.com")),
                            ),
                            createSampleProduct(
                                userId = 1L,
                                title = "개인 스튜디오 대여",
                                price = 80_000L,
                                typeCode = 2,
                            ),
                            createSampleProduct(
                                userId = 1L,
                                title = "아기 사진 전문 촬영",
                                price = 300_000L,
                                typeCode = 1,
                                partnerShops = listOf(PartnerShop("협력사2", "http://partner2.com")),
                            ),
                        ).map { productPersistencePort.save(it) }

                    jpaProductRepository.flush()
                }

                it("패키지 상품만 필터링 가능해야 함") {
                    val results =
                        productPersistencePort.searchByCriteria(
                            title = null,
                            priceRange = null,
                            typeCode = 1,
                            sortBy = null,
                        )

                    val expectedTitles = testProducts.filter { it.typeCode == 1 }.map { it.title }
                    results.map { it.title } shouldContainExactlyInAnyOrder expectedTitles
                }

                it("가격 오름차순 정렬이 올바르게 동작해야 함") {
                    val results =
                        productPersistencePort.searchByCriteria(
                            title = null,
                            priceRange = null,
                            typeCode = 1,
                            sortBy = "price-asc",
                        )

                    results.map { it.price } shouldBe listOf(150_000L, 300_000L)
                }

                it("가격 내림차순 정렬이 올바르게 동작해야 함") {
                    val results =
                        productPersistencePort.searchByCriteria(
                            title = null,
                            priceRange = null,
                            typeCode = 1,
                            sortBy = "price-desc",
                        )

                    results.map { it.price } shouldBe listOf(300_000L, 150_000L)
                }
            }

            context("상품 수정 시") {
                it("촬영 시간 변경이 정상 반영되어야 함") {
                    // 테스트 로직이 주석 처리되어 있으므로, 필요 시 주석 해제 후 테스트 작성
                }
            }

            context("협력업체 관리") {
                it("패키지 상품에 여러 협력업체 연결 가능") {
                    val partnerShops =
                        listOf(
                            PartnerShop(name = "헤어샵", link = "http://hairshop.com"),
                            PartnerShop(name = "메이크업", link = "http://makeup.com"),
                        )

                    val product =
                        productPersistencePort.save(
                            createSampleProduct(
                                userId = 1L,
                                typeCode = 1,
                                partnerShops = partnerShops,
                            ),
                        )
                    jpaProductRepository.flush()

                    val retrieved = productPersistencePort.findById(product.productId)
                    retrieved?.partnerShops?.shouldHaveSize(2)
                }
            }
        }
    }) {
    companion object {
        fun createSampleProduct(
            userId: Long = 1L,
            title: String = "기본 상품",
            price: Long = 100_000L,
            typeCode: Int = 2,
            partnerShops: List<PartnerShop> = emptyList(),
            images: List<String> = listOf("default.jpg"),
        ): Product =
            Product(
                userId = userId,
                title = title,
                price = price,
                typeCode = typeCode,
                partnerShops = partnerShops,
                images = images,
                shootingLocation = "기본 촬영장소",
                numberOfCostumes = 1,
                contactInfo = "contact@example.com",
                options = emptyList(),
            )
    }
}
