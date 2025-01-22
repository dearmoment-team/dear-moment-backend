package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.time.LocalDateTime

@DataJpaTest
@Import(ProductOptionRepositoryAdapterTest.TestConfig::class, ProductOptionRepositoryAdapter::class)
class ProductOptionRepositoryAdapterTest(
    @Autowired val productOptionRepositoryAdapter: ProductOptionRepositoryAdapter,
    @Autowired val jpaProductOptionRepository: JpaProductOptionRepository,
    @Autowired val productEntityRetrievalPort: ProductEntityRetrievalPort
) : StringSpec() {

    override fun extensions() = listOf(SpringExtension)

    @Configuration
    class TestConfig {
        @Bean
        fun productEntityRetrievalPort(): ProductEntityRetrievalPort = mockk()
    }

    init {
        "옵션을 저장하고 다시 조회할 수 있어야 한다" {
            // Given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)

            val productId = 1L
            val productEntity = ProductEntity(
                productId = productId,
                userId = 1L,
                title = "테스트 상품",
                description = "테스트 설명",
                price = 100_000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow
            )

            // Mocking the retrieval of ProductEntity
            every { productEntityRetrievalPort.getProductEntityById(productId) } returns productEntity

            val productOption = ProductOption(
                optionId = 0L,
                name = "테스트 옵션",
                additionalPrice = 5_000,
                description = "테스트 옵션 설명",
                productId = productId,
                createdAt = fixedNow,
                updatedAt = fixedNow
            )

            // When
            val savedOption = productOptionRepositoryAdapter.save(productOption)
            val foundOption = productOptionRepositoryAdapter.findById(savedOption.optionId)

            // Then
            foundOption.optionId shouldBe savedOption.optionId
            foundOption.name shouldBe productOption.name
            foundOption.additionalPrice shouldBe productOption.additionalPrice
            foundOption.description shouldBe productOption.description
            foundOption.productId shouldBe productOption.productId
            foundOption.createdAt shouldBe fixedNow
            foundOption.updatedAt shouldBe fixedNow
        }

        "ID로 옵션을 조회할 수 있어야 한다" {
            // Given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)

            val productId = 2L
            val productEntity = ProductEntity(
                productId = productId,
                userId = 2L,
                title = "엔티티 조회 상품",
                description = "엔티티 조회 설명",
                price = 150_000,
                typeCode = 2,
                createdAt = fixedNow,
                updatedAt = fixedNow
            )

            every { productEntityRetrievalPort.getProductEntityById(productId) } returns productEntity

            val productOption = ProductOption(
                optionId = 0L,
                name = "기본 옵션",
                additionalPrice = 5_000,
                description = "기본 옵션 설명",
                productId = productId,
                createdAt = fixedNow,
                updatedAt = fixedNow
            )

            val savedOption = productOptionRepositoryAdapter.save(productOption)

            // When
            val retrievedOption = productOptionRepositoryAdapter.findById(savedOption.optionId)

            // Then
            retrievedOption.optionId shouldBe savedOption.optionId
            retrievedOption.name shouldBe "기본 옵션"
            retrievedOption.additionalPrice shouldBe 5_000
            retrievedOption.description shouldBe "기본 옵션 설명"
            retrievedOption.productId shouldBe productId
        }

        "모든 옵션을 조회할 수 있어야 한다" {
            // Given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)

            val productId = 3L
            val productEntity = ProductEntity(
                productId = productId,
                userId = 3L,
                title = "모든 조회 상품",
                description = "모든 조회 설명",
                price = 200_000,
                typeCode = 3,
                createdAt = fixedNow,
                updatedAt = fixedNow
            )

            every { productEntityRetrievalPort.getProductEntityById(productId) } returns productEntity

            val option1 = ProductOption(
                optionId = 0L,
                name = "옵션 A",
                additionalPrice = 10_000,
                description = "옵션 A 설명",
                productId = productId,
                createdAt = fixedNow,
                updatedAt = fixedNow
            )

            val option2 = ProductOption(
                optionId = 0L,
                name = "옵션 B",
                additionalPrice = 20_000,
                description = "옵션 B 설명",
                productId = productId,
                createdAt = fixedNow,
                updatedAt = fixedNow
            )

            productOptionRepositoryAdapter.save(option1)
            productOptionRepositoryAdapter.save(option2)

            // When
            val allOptions = productOptionRepositoryAdapter.findAll()

            // Then
            allOptions shouldHaveSize 2
            allOptions.map { it.name } shouldContainAll listOf("옵션 A", "옵션 B")
        }

        "존재하지 않는 ID로 조회 시 예외가 발생해야 한다" {
            // Given
            val nonExistentId = 999L

            // When & Then
            val exception = shouldThrow<IllegalArgumentException> {
                productOptionRepositoryAdapter.findById(nonExistentId)
            }
            exception.message shouldBe "ProductOption with ID $nonExistentId not found"
        }

        "옵션을 ID로 삭제할 수 있어야 한다" {
            // Given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)

            val productId = 4L
            val productEntity = ProductEntity(
                productId = productId,
                userId = 4L,
                title = "삭제 테스트 상품",
                description = "삭제 테스트 설명",
                price = 250_000,
                typeCode = 4,
                createdAt = fixedNow,
                updatedAt = fixedNow
            )

            every { productEntityRetrievalPort.getProductEntityById(productId) } returns productEntity

            val productOption = ProductOption(
                optionId = 0L,
                name = "삭제 옵션",
                additionalPrice = 15_000,
                description = "삭제 옵션 설명",
                productId = productId,
                createdAt = fixedNow,
                updatedAt = fixedNow
            )

            val savedOption = productOptionRepositoryAdapter.save(productOption)

            // When
            productOptionRepositoryAdapter.deleteById(savedOption.optionId)

            // Then
            val exception = shouldThrow<IllegalArgumentException> {
                productOptionRepositoryAdapter.findById(savedOption.optionId)
            }
            exception.message shouldBe "ProductOption with ID ${savedOption.optionId} not found"
        }
    }
}