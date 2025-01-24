package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDateTime

@DataJpaTest
@Import(ProductRepositoryAdapter::class)
class ProductRepositoryAdapterTest(
    private val productRepositoryAdapter: ProductRepositoryAdapter,
) : StringSpec() {
    init {
        "상품과 옵션을 저장 후 다시 조회해본다" {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0) // 고정된 시간 사용

            val options =
                listOf(
                    kr.kro.dearmoment.product.domain.model.ProductOption(
                        optionId = 0L,
                        name = "익스프레스 서비스",
                        additionalPrice = 80000,
                        description = "최대 1주~3주 내 보정본 전달",
                        productId = 0L,
                        createdAt = fixedNow,
                        updatedAt = fixedNow,
                    ),
                    kr.kro.dearmoment.product.domain.model.ProductOption(
                        optionId = 0L,
                        name = "세부 보정 추가",
                        additionalPrice = 10000,
                        description = "세부 보정 15장 제공",
                        productId = 0L,
                        createdAt = fixedNow,
                        updatedAt = fixedNow,
                    ),
                )

            val product =
                kr.kro.dearmoment.product.domain.model.Product(
                    productId = 0L,
                    userId = 1L,
                    title = "옵션 테스트 상품",
                    description = "옵션 포함 테스트 설명",
                    price = 100000,
                    typeCode = 1,
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                    options = options,
                )

            // when
            val savedProduct = productRepositoryAdapter.save(product)
            val foundProduct = productRepositoryAdapter.findById(savedProduct.productId)

            // then
            foundProduct.productId shouldBe savedProduct.productId
            foundProduct.userId shouldBe product.userId
            foundProduct.title shouldBe product.title
            foundProduct.description shouldBe product.description
            foundProduct.price shouldBe product.price
            foundProduct.typeCode shouldBe product.typeCode
            foundProduct.createdAt shouldBe fixedNow
            foundProduct.updatedAt shouldBe fixedNow
            foundProduct.options shouldHaveSize 2

            // 옵션 검증
            val expressOption = foundProduct.options.find { it.name == "익스프레스 서비스" }
            expressOption.shouldNotBeNull()
            expressOption.additionalPrice shouldBe 80000
            expressOption.description shouldBe "최대 1주~3주 내 보정본 전달"

            val detailedOption = foundProduct.options.find { it.name == "세부 보정 추가" }
            detailedOption.shouldNotBeNull()
            detailedOption.additionalPrice shouldBe 10000
            detailedOption.description shouldBe "세부 보정 15장 제공"
        }

        "상품 엔티티를 ID로 조회할 수 있다" {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0) // 고정된 시간 사용

            val options =
                listOf(
                    kr.kro.dearmoment.product.domain.model.ProductOption(
                        optionId = 0L,
                        name = "기본 옵션",
                        additionalPrice = 5000,
                        description = "기본 옵션 설명",
                        productId = 0L,
                        createdAt = fixedNow,
                        updatedAt = fixedNow,
                    ),
                )

            val product =
                kr.kro.dearmoment.product.domain.model.Product(
                    productId = 0L,
                    userId = 1L,
                    title = "엔티티 조회 테스트",
                    description = "엔티티 조회 설명",
                    price = 150000,
                    typeCode = 2,
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                    options = options,
                )

            // 엔티티 저장
            val savedProduct = productRepositoryAdapter.save(product)

            // when
            val retrievedEntity = productRepositoryAdapter.getProductEntityById(savedProduct.productId)

            // then
            retrievedEntity.productId shouldBe savedProduct.productId
            retrievedEntity.userId shouldBe savedProduct.userId
            retrievedEntity.title shouldBe savedProduct.title
            retrievedEntity.description shouldBe savedProduct.description
            retrievedEntity.price shouldBe savedProduct.price
            retrievedEntity.typeCode shouldBe savedProduct.typeCode
            retrievedEntity.createdAt shouldBe fixedNow
            retrievedEntity.updatedAt shouldBe fixedNow
            retrievedEntity.options shouldHaveSize 1

            val retrievedOption = retrievedEntity.options.first()
            retrievedOption.name shouldBe "기본 옵션"
            retrievedOption.additionalPrice shouldBe 5000
            retrievedOption.description shouldBe "기본 옵션 설명"
            retrievedOption.product.productId shouldBe savedProduct.productId
        }

        "모든 상품을 조회할 수 있다" {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0) // 고정된 시간 사용

            val product1 =
                kr.kro.dearmoment.product.domain.model.Product(
                    productId = 0L,
                    userId = 1L,
                    title = "상품 1",
                    description = "상품 1 설명",
                    price = 100000,
                    typeCode = 1,
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                    options = emptyList(),
                )

            val product2 =
                kr.kro.dearmoment.product.domain.model.Product(
                    productId = 0L,
                    userId = 2L,
                    title = "상품 2",
                    description = "상품 2 설명",
                    price = 200000,
                    typeCode = 2,
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                    options =
                    listOf(
                        kr.kro.dearmoment.product.domain.model.ProductOption(
                            optionId = 0L,
                            name = "옵션 A",
                            additionalPrice = 15000,
                            description = "옵션 A 설명",
                            productId = 0L,
                            createdAt = fixedNow,
                            updatedAt = fixedNow,
                        ),
                    ),
                )

            // 저장
            productRepositoryAdapter.save(product1)
            productRepositoryAdapter.save(product2)

            // when
            val allProducts = productRepositoryAdapter.findAll()

            // then
            allProducts shouldHaveSize 2
            allProducts.map { it.title } shouldContainAll listOf("상품 1", "상품 2")

            val retrievedProduct2 = allProducts.find { it.title == "상품 2" }
            retrievedProduct2.shouldNotBeNull()
            retrievedProduct2.options.shouldHaveSize(1)
            val optionA = retrievedProduct2.options.first()
            optionA.name shouldBe "옵션 A"
            optionA.additionalPrice shouldBe 15000
            optionA.description shouldBe "옵션 A 설명"
        }

        "존재하지 않는 ID로 조회 시 예외가 발생한다" {
            // given
            val nonExistentId = 999L

            // when & then
            val exception =
                shouldThrow<IllegalArgumentException> {
                    productRepositoryAdapter.findById(nonExistentId)
                }
            exception.message shouldBe "Product with ID $nonExistentId not found"
        }

        "존재하지 않는 ID로 엔티티 조회 시 예외가 발생한다" {
            // given
            val nonExistentId = 999L

            // when & then
            val exception =
                shouldThrow<IllegalArgumentException> {
                    productRepositoryAdapter.getProductEntityById(nonExistentId)
                }
            exception.message shouldBe "Product with ID $nonExistentId not found"
        }

        "중복된 옵션 이름 저장 시 예외 발생" {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val duplicateOptions = listOf(
                ProductOption(
                    optionId = 0L,
                    name = "중복 옵션",
                    additionalPrice = 5000,
                    description = "중복 옵션 설명",
                    productId = 0L,
                    createdAt = fixedNow,
                    updatedAt = fixedNow
                ),
                ProductOption(
                    optionId = 0L,
                    name = "중복 옵션",
                    additionalPrice = 10000,
                    description = "또 다른 중복 옵션 설명",
                    productId = 0L,
                    createdAt = fixedNow,
                    updatedAt = fixedNow
                )
            )
            val product = Product(
                productId = 0L,
                userId = 3L,
                title = "중복 옵션 테스트 상품",
                description = "중복 옵션 포함 상품 설명",
                price = 200000,
                typeCode = 3,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = duplicateOptions
            )

            // when & then
            val exception = shouldThrow<IllegalArgumentException> {
                productRepositoryAdapter.save(product)
            }
            exception.message shouldBe "Duplicate option name found in product: 중복 옵션"
        }

        "상품 정보를 업데이트할 수 있다" {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val product = Product(
                productId = 0L,
                userId = 1L,
                title = "초기 상품 제목",
                description = "초기 상품 설명",
                price = 50000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = emptyList()
            )
            val savedProduct = productRepositoryAdapter.save(product)

            // when
            val updatedProduct = savedProduct.copy(
                title = "업데이트된 상품 제목",
                description = "업데이트된 상품 설명",
                price = 60000,
                updatedAt = fixedNow.plusDays(1)
            )
            productRepositoryAdapter.save(updatedProduct)
            val retrievedProduct = productRepositoryAdapter.findById(savedProduct.productId)

            // then
            retrievedProduct.title shouldBe "업데이트된 상품 제목"
            retrievedProduct.description shouldBe "업데이트된 상품 설명"
            retrievedProduct.price shouldBe 60000
            retrievedProduct.updatedAt shouldBe fixedNow.plusDays(1)
        }

        "옵션을 추가하거나 삭제할 수 있다" {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val product = Product(
                productId = 0L,
                userId = 1L,
                title = "옵션 테스트 상품",
                description = "옵션 테스트 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = listOf(
                    ProductOption(
                        optionId = 0L,
                        name = "초기 옵션",
                        additionalPrice = 5000,
                        description = "초기 옵션 설명",
                        productId = 0L,
                        createdAt = fixedNow,
                        updatedAt = fixedNow
                    )
                )
            )
            val savedProduct = productRepositoryAdapter.save(product)

            // when
            val updatedProduct = savedProduct.copy(
                options = listOf(
                    ProductOption(
                        optionId = 0L,
                        name = "새로운 옵션",
                        additionalPrice = 15000,
                        description = "새로운 옵션 설명",
                        productId = savedProduct.productId,
                        createdAt = fixedNow.plusDays(1),
                        updatedAt = fixedNow.plusDays(1)
                    )
                )
            )
            productRepositoryAdapter.save(updatedProduct)
            val retrievedProduct = productRepositoryAdapter.findById(savedProduct.productId)

            // then
            retrievedProduct.options shouldHaveSize 1
            retrievedProduct.options.first().name shouldBe "새로운 옵션"
            retrievedProduct.options.first().additionalPrice shouldBe 15000
        }

        "옵션이 없는 상품을 조회할 수 있다" {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val product = Product(
                productId = 0L,
                userId = 1L,
                title = "옵션 없는 상품",
                description = "옵션 없는 상품 설명",
                price = 50000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = emptyList()
            )
            val savedProduct = productRepositoryAdapter.save(product)

            // when
            val retrievedProduct = productRepositoryAdapter.findById(savedProduct.productId)

            // then
            retrievedProduct.options shouldHaveSize 0
        }
    }
}
