package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@Import(ProductOptionRepositoryAdapter::class)
@ActiveProfiles("test")
class ProductOptionRepositoryAdapterTest(
    @Autowired private val productOptionPersistencePort: ProductOptionPersistencePort,
    @Autowired private val jpaProductRepository: JpaProductRepository,
    @Autowired private val jpaProductOptionRepository: JpaProductOptionRepository,
) : DescribeSpec({

        describe("ProductOptionRepositoryAdapter 테스트") {
            lateinit var testProductEntity: ProductEntity
            lateinit var testProductDomain: Product

            beforeEach {
                // 기본 상품 생성 (Entity)
                testProductEntity =
                    jpaProductRepository.save(
                        ProductEntity(
                            title = "테스트 상품",
                            price = 100_000L,
                            typeCode = 1,
                            images = listOf("image.jpg"),
                            partnerShops =
                                listOf(
                                    PartnerShopEmbeddable("협력업체명", "https://partner.link"),
                                ),
                        ),
                    )
                // Entity → Domain 변환
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

                    // When (Domain 모델 전달)
                    val saved =
                        productOptionPersistencePort.save(
                            option,
                            testProductDomain,
                        )

                    // Then
                    with(saved) {
                        optionId shouldNotBe null
                        name shouldBe "옵션1"
                        additionalPrice shouldBe 10_000L
                    }
                }

                it("동일 상품에 중복 이름의 옵션 저장 시 예외 발생") {
                    // Given
                    val option1 = createSampleOption("중복옵션", 5_000L)
                    productOptionPersistencePort.save(option1, testProductDomain)

                    val option2 = createSampleOption("중복옵션", 7_000L)

                    // When & Then
                    shouldThrow<IllegalArgumentException> {
                        productOptionPersistencePort.save(option2, testProductDomain)
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
                            // Domain 전달
                            productOptionPersistencePort.save(it, testProductDomain)
                        }
                }

                it("ID로 옵션 조회 성공") {
                    // When
                    val found = productOptionPersistencePort.findById(savedOptions.first().optionId!!)

                    // Then
                    with(found) {
                        name shouldBe "옵션A"
                        additionalPrice shouldBe 10_000L
                    }
                }

                it("존재하지 않는 ID 조회 시 예외 발생") {
                    shouldThrow<IllegalArgumentException> {
                        productOptionPersistencePort.findById(9999)
                    }.message shouldBe "ProductOption with ID 9999 not found"
                }

                it("상품 기준 옵션 조회 성공") {
                    // When
                    val found = productOptionPersistencePort.findByProductId(testProductDomain.productId!!)

                    // Then
                    found.map { it.name } shouldContainExactlyInAnyOrder listOf("옵션A", "옵션B")
                }
            }

            context("옵션 삭제 기능") {
                lateinit var targetOption: ProductOption

                beforeEach {
                    targetOption =
                        productOptionPersistencePort.save(
                            createSampleOption("삭제대상", 15_000L),
                            testProductDomain,
                        )
                }

                it("ID로 옵션 삭제 성공") {
                    // When
                    productOptionPersistencePort.deleteById(targetOption.optionId!!)

                    // Then
                    jpaProductOptionRepository.count() shouldBe 0
                }

                it("상품ID 기준 전체 삭제 성공") {
                    // Given
                    productOptionPersistencePort.save(createSampleOption("추가옵션", 5_000L), testProductDomain)

                    // When
                    productOptionPersistencePort.deleteAllByProductId(testProductDomain.productId!!)

                    // Then
                    jpaProductOptionRepository.count() shouldBe 0
                }
            }

            context("기타 기능") {
                it("상품 옵션 존재 여부 확인") {
                    // Before
                    productOptionPersistencePort.existsByProductId(testProductDomain.productId!!) shouldBe false

                    // After save
                    productOptionPersistencePort.save(createSampleOption("확인옵션", 9_000L), testProductDomain)
                    productOptionPersistencePort.existsByProductId(testProductDomain.productId!!) shouldBe true
                }

                it("전체 옵션 조회") {
                    // Given
                    listOf("A", "B", "C").forEach {
                        productOptionPersistencePort.save(createSampleOption(it, 1000L), testProductDomain)
                    }

                    // When
                    val allOptions = productOptionPersistencePort.findAll()

                    // Then
                    allOptions shouldHaveSize 3
                }
            }
        }
    }) {
    companion object {
        fun createSampleOption(
            name: String = "기본옵션",
            price: Long = 0L,
            createdAt: LocalDateTime = LocalDateTime.now(),
        ): ProductOption =
            ProductOption(
                name = name,
                additionalPrice = price,
                description = "옵션 설명",
                createdAt = createdAt,
            )
    }
}
