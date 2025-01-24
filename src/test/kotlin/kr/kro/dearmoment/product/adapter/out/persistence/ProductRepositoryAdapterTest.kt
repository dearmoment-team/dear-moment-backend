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
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@DataJpaTest
@Transactional
@Import(ProductRepositoryAdapter::class)
class ProductRepositoryAdapterTest(
    private val productRepositoryAdapter: ProductRepositoryAdapter,
) : StringSpec() {
    init {
        "상품 엔티티를 ID로 조회할 수 있다" {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0) // 고정된 시간 사용

            val options =
                listOf(
                    ProductOption(
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
                Product(
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
                Product(
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
                Product(
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
                            ProductOption(
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
    }
}
