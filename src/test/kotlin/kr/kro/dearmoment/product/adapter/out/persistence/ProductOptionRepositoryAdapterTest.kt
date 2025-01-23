package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.time.LocalDateTime

@DataJpaTest
@Import(ProductOptionRepositoryAdapterTest.TestConfig::class, ProductOptionRepositoryAdapter::class)
@EntityScan(basePackages = ["kr.kro.dearmoment.product.domain.model", "kr.kro.dearmoment.product.adapter.out.persistence"])
@EnableJpaRepositories(basePackages = ["kr.kro.dearmoment.product.adapter.out.persistence"])
class ProductOptionRepositoryAdapterTest(
    private val productOptionRepositoryAdapter: ProductOptionRepositoryAdapter,
    private val jpaProductOptionRepository: JpaProductOptionRepository,
    private val jpaProductRepository: JpaProductRepository,
    private val productEntityRetrievalPort: ProductEntityRetrievalPort,
) : StringSpec() {
    @Configuration
    class TestConfig {
        @Bean
        fun productEntityRetrievalPort(jpaProductRepository: JpaProductRepository): ProductEntityRetrievalPort {
            val mockPort = mockk<ProductEntityRetrievalPort>()
            every { mockPort.getProductEntityById(any()) } answers {
                val id = firstArg<Long>()
                jpaProductRepository.findById(id).orElseThrow { IllegalArgumentException("Product with ID $id not found") }
            }
            return mockPort
        }
    }

    init {
        "옵션을 저장하고 다시 조회할 수 있어야 한다" {
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)

            val productEntity =
                ProductEntity(
                    productId = null,
                    userId = 1L,
                    title = "테스트 상품",
                    description = "테스트 설명",
                    price = 100_000,
                    typeCode = 1,
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                )

            val savedProductEntity = jpaProductRepository.save(productEntity)

            val productOption =
                ProductOption(
                    optionId = 0L,
                    name = "테스트 옵션",
                    additionalPrice = 5_000,
                    description = "테스트 옵션 설명",
                    productId = savedProductEntity.productId!!,
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                )

            val savedOption = productOptionRepositoryAdapter.save(productOption)
            val foundOption = productOptionRepositoryAdapter.findById(savedOption.optionId)

            foundOption.optionId shouldBe savedOption.optionId
            foundOption.name shouldBe productOption.name
            foundOption.additionalPrice shouldBe productOption.additionalPrice
            foundOption.description shouldBe productOption.description
            foundOption.productId shouldBe productOption.productId
            foundOption.createdAt shouldBe fixedNow
            foundOption.updatedAt shouldBe fixedNow
        }

        "ID로 옵션을 조회할 수 있어야 한다" {
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)

            val productEntity =
                ProductEntity(
                    productId = null,
                    userId = 2L,
                    title = "엔티티 조회 상품",
                    description = "엔티티 조회 설명",
                    price = 150_000,
                    typeCode = 2,
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                )

            val savedProductEntity = jpaProductRepository.save(productEntity)

            val productOption =
                ProductOption(
                    optionId = 0L,
                    name = "기본 옵션",
                    additionalPrice = 5_000,
                    description = "기본 옵션 설명",
                    productId = savedProductEntity.productId!!,
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                )

            val savedOption = productOptionRepositoryAdapter.save(productOption)

            val retrievedOption = productOptionRepositoryAdapter.findById(savedOption.optionId)

            retrievedOption.optionId shouldBe savedOption.optionId
            retrievedOption.name shouldBe "기본 옵션"
            retrievedOption.additionalPrice shouldBe 5_000
            retrievedOption.description shouldBe "기본 옵션 설명"
            retrievedOption.productId shouldBe savedProductEntity.productId!!
        }

        "모든 옵션을 조회할 수 있어야 한다" {
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)

            val productEntity =
                ProductEntity(
                    productId = null,
                    userId = 3L,
                    title = "모든 조회 상품",
                    description = "모든 조회 설명",
                    price = 200_000,
                    typeCode = 3,
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                )

            val savedProductEntity = jpaProductRepository.save(productEntity)

            val option1 =
                ProductOption(
                    optionId = 0L,
                    name = "옵션 A",
                    additionalPrice = 10_000,
                    description = "옵션 A 설명",
                    productId = savedProductEntity.productId!!,
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                )

            val option2 =
                ProductOption(
                    optionId = 0L,
                    name = "옵션 B",
                    additionalPrice = 20_000,
                    description = "옵션 B 설명",
                    productId = savedProductEntity.productId!!,
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                )

            productOptionRepositoryAdapter.save(option1)
            productOptionRepositoryAdapter.save(option2)

            val allOptions = productOptionRepositoryAdapter.findAll()

            allOptions shouldHaveSize 2
            allOptions.map { it.name } shouldContainAll listOf("옵션 A", "옵션 B")
        }

        "존재하지 않는 ID로 조회 시 예외가 발생해야 한다" {
            val nonExistentId = 999L

            val exception =
                shouldThrow<IllegalArgumentException> {
                    productOptionRepositoryAdapter.findById(nonExistentId)
                }
            exception.message shouldBe "ProductOption with ID $nonExistentId not found"
        }

        "옵션을 ID로 삭제할 수 있어야 한다" {
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)

            val productEntity =
                ProductEntity(
                    productId = null,
                    userId = 4L,
                    title = "삭제 테스트 상품",
                    description = "삭제 테스트 설명",
                    price = 250_000,
                    typeCode = 4,
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                )

            val savedProductEntity = jpaProductRepository.save(productEntity)

            val productOption =
                ProductOption(
                    optionId = 0L,
                    name = "삭제 옵션",
                    additionalPrice = 15_000,
                    description = "삭제 옵션 설명",
                    productId = savedProductEntity.productId!!,
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                )

            val savedOption = productOptionRepositoryAdapter.save(productOption)

            productOptionRepositoryAdapter.deleteById(savedOption.optionId)

            val exception =
                shouldThrow<IllegalArgumentException> {
                    productOptionRepositoryAdapter.findById(savedOption.optionId)
                }
            exception.message shouldBe "ProductOption with ID ${savedOption.optionId} not found"
        }
    }
}
