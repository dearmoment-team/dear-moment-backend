package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.product.domain.model.OptionType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption

@RepositoryTest
class ProductOptionRepositoryAdapterTest(
    private val jpaProductRepository: JpaProductRepository,
    private val jpaProductOptionRepository: JpaProductOptionRepository,
) : DescribeSpec({
        val adapter = ProductOptionRepositoryAdapter(jpaProductOptionRepository, jpaProductRepository)

        describe("ProductOptionRepositoryAdapter 테스트") {
            lateinit var testProductEntity: ProductEntity
            lateinit var testProductDomain: Product

            beforeEach {
                // 테스트용 ProductEntity 생성
                testProductEntity =
                    jpaProductRepository.save(
                        ProductEntity(
                            userId = 1L,
                            // 변경된 도메인: price, typeCode, images, partnerShops 등이 제거되고,
                            // productType와 shootingPlace가 추가됨.
                            productType = kr.kro.dearmoment.product.domain.model.ProductType.WEDDING_SNAP,
                            shootingPlace = kr.kro.dearmoment.product.domain.model.ShootingPlace.JEJU,
                            title = "테스트 상품",
                            mainImage =
                                ImageEmbeddable.fromDomainImage(
                                    kr.kro.dearmoment.image.domain.Image(
                                        userId = 1L,
                                        fileName = "main.jpg",
                                        url = "http://example.com/main.jpg",
                                    ),
                                ),
                            subImages =
                                listOf(
                                    ImageEmbeddable.fromDomainImage(
                                        kr.kro.dearmoment.image.domain.Image(
                                            userId = 1L,
                                            fileName = "sub1.jpg",
                                            url = "http://example.com/sub1.jpg",
                                        ),
                                    ),
                                    ImageEmbeddable.fromDomainImage(
                                        kr.kro.dearmoment.image.domain.Image(
                                            userId = 1L,
                                            fileName = "sub2.jpg",
                                            url = "http://example.com/sub2.jpg",
                                        ),
                                    ),
                                    ImageEmbeddable.fromDomainImage(
                                        kr.kro.dearmoment.image.domain.Image(
                                            userId = 1L,
                                            fileName = "sub3.jpg",
                                            url = "http://example.com/sub3.jpg",
                                        ),
                                    ),
                                    ImageEmbeddable.fromDomainImage(
                                        kr.kro.dearmoment.image.domain.Image(
                                            userId = 1L,
                                            fileName = "sub4.jpg",
                                            url = "http://example.com/sub4.jpg",
                                        ),
                                    ),
                                ).toMutableList(),
                            additionalImages = emptyList<ImageEmbeddable>().toMutableList(),
                        ),
                    )
                testProductDomain = testProductEntity.toDomain()
                jpaProductOptionRepository.deleteAllInBatch()
            }

            afterEach {
                jpaProductOptionRepository.deleteAllInBatch()
                jpaProductRepository.deleteAllInBatch()
            }

            context("옵션 저장 기능") {
                it("새로운 옵션을 정상 저장해야 함") {
                    // Given
                    val option = createSampleOption("옵션1", 10_000L)
                    // When (도메인 모델 전달)
                    val saved: ProductOption = adapter.save(option, testProductDomain)
                    jpaProductOptionRepository.flush()
                    val persisted = jpaProductOptionRepository.findById(saved.optionId).orElse(null)
                    with(persisted!!) {
                        optionId shouldNotBe 0L
                        name shouldBe "옵션1"
                        originalPrice shouldBe 10_000L
                        // Auditing 필드는 createdDate, updateDate로 관리됨
                        createdDate shouldNotBe null
                        updateDate shouldNotBe null
                    }
                }

                it("동일 상품에 중복 이름의 옵션 저장 시 예외 발생") {
                    // Given
                    val option1 = createSampleOption("중복옵션", 5_000L)
                    adapter.save(option1, testProductDomain)
                    val option2 = createSampleOption("중복옵션", 7_000L)
                    // When & Then
                    shouldThrow<IllegalArgumentException> {
                        adapter.save(option2, testProductDomain)
                    }.message shouldBe "ProductOption already exists: 중복옵션"
                }
            }

            context("옵션 조회 기능") {
                lateinit var savedOptions: List<ProductOption>

                beforeEach {
                    savedOptions =
                        listOf(
                            createSampleOption("옵션A", 10_000L),
                            createSampleOption("옵션B", 20_000L),
                        ).map {
                            adapter.save(it, testProductDomain)
                        }
                    jpaProductOptionRepository.flush()
                }

                it("ID로 옵션 조회 성공") {
                    // When
                    val found: ProductOption = adapter.findById(savedOptions.first().optionId)
                    // Then
                    with(found) {
                        name shouldBe "옵션A"
                        originalPrice shouldBe 10_000L
                    }
                }

                it("존재하지 않는 ID 조회 시 예외 발생") {
                    shouldThrow<IllegalArgumentException> {
                        adapter.findById(9999)
                    }.message shouldBe "ProductOption with ID 9999 not found"
                }

                it("상품 기준 옵션 조회 성공") {
                    // When
                    val found = adapter.findByProductId(testProductDomain.productId)
                    // Then
                    found.map { it.name } shouldContainExactlyInAnyOrder listOf("옵션A", "옵션B")
                }
            }

            context("옵션 삭제 기능") {
                lateinit var targetOption: ProductOption

                beforeEach {
                    targetOption = adapter.save(createSampleOption("삭제대상", 15_000L), testProductDomain)
                    jpaProductOptionRepository.flush()
                }

                it("ID로 옵션 삭제 성공") {
                    // When
                    adapter.deleteById(targetOption.optionId)
                    jpaProductOptionRepository.flush()
                    // Then
                    jpaProductOptionRepository.count() shouldBe 0
                }

                it("상품ID 기준 전체 삭제 성공") {
                    // Given
                    adapter.save(createSampleOption("추가옵션", 5_000L), testProductDomain)
                    jpaProductOptionRepository.flush()
                    // When
                    adapter.deleteAllByProductId(testProductDomain.productId)
                    jpaProductOptionRepository.flush()
                    // Then
                    jpaProductOptionRepository.count() shouldBe 0
                }
            }

            context("기타 기능") {
                it("상품 옵션 존재 여부 확인") {
                    // Before
                    adapter.existsByProductId(testProductDomain.productId) shouldBe false
                    // After save
                    adapter.save(createSampleOption("확인옵션", 9_000L), testProductDomain)
                    jpaProductOptionRepository.flush()
                    adapter.existsByProductId(testProductDomain.productId) shouldBe true
                }

                it("전체 옵션 조회") {
                    // Given
                    listOf("A", "B", "C").forEach {
                        adapter.save(createSampleOption(it, 1000L), testProductDomain)
                    }
                    jpaProductOptionRepository.flush()
                    // When
                    val allOptions = adapter.findAll()
                    // Then
                    allOptions shouldHaveSize 3
                }
            }
        }
    }) {
    companion object {
        fun createSampleOption(
            name: String = "기본옵션",
            originalPrice: Long = 0L,
        ): ProductOption =
            ProductOption(
                optionId = 0L,
                productId = 0L,
                name = name,
                optionType = OptionType.SINGLE,
                discountAvailable = false,
                originalPrice = originalPrice,
                discountPrice = 0L,
                description = "옵션 설명",
                costumeCount = 1,
                shootingLocationCount = 1,
                shootingHours = 0,
                shootingMinutes = 30,
                retouchedCount = 1,
                partnerShops = emptyList(),
            )
    }
}
