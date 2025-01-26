package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.common.TestConfig
import kr.kro.dearmoment.common.TestObjectFactory
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.time.LocalDateTime

@DataJpaTest
@Import(TestConfig::class)
@EntityScan(basePackages = ["kr.kro.dearmoment.product.adapter.out.persistence"])
@EnableJpaRepositories(basePackages = ["kr.kro.dearmoment.product.adapter.out.persistence"])
class ProductOptionRepositoryAdapterTest(
    private val productOptionRepositoryAdapter: ProductOptionRepositoryAdapter,
    private val testObjectFactory: TestObjectFactory
) : StringSpec() {

    init {
        val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)

        "옵션을 저장하고 다시 조회할 수 있어야 한다" {
            // Given
            val productEntity = testObjectFactory.createTestProductEntity(fixedNow)
            val savedProductEntity = testObjectFactory.saveProductEntity(productEntity)

            val productOption = testObjectFactory.createTestProductOptionDomain(
                fixedNow = fixedNow,
                productId = requireNotNull(savedProductEntity.productId) { "Product ID must not be null" },
                name = "테스트 옵션",
                additionalPrice = 5000L
            )
            val savedOption = productOptionRepositoryAdapter.save(productOption)

            // When
            val foundOption = productOptionRepositoryAdapter.findById(
                requireNotNull(savedOption.optionId) { "Option ID must not be null" }
            )

            // Then
            foundOption shouldBe savedOption
        }

        "ID로 옵션을 조회할 수 있어야 한다" {
            // Given
            val productEntity = testObjectFactory.createTestProductEntity(fixedNow)
            val savedProductEntity = testObjectFactory.saveProductEntity(productEntity)

            val productOption = testObjectFactory.createTestProductOptionDomain(
                fixedNow = fixedNow,
                productId = requireNotNull(savedProductEntity.productId) { "Product ID must not be null" },
                name = "테스트 옵션",
                additionalPrice = 5000L
            )
            val savedOption = productOptionRepositoryAdapter.save(productOption)

            // When
            val retrievedOption = productOptionRepositoryAdapter.findById(
                requireNotNull(savedOption.optionId) { "Option ID must not be null" }
            )

            // Then
            retrievedOption shouldBe savedOption
        }

        "모든 옵션을 조회할 수 있어야 한다" {
            // Given
            val productEntity = testObjectFactory.createTestProductEntity(fixedNow)
            val savedProductEntity = testObjectFactory.saveProductEntity(productEntity)

            val options = listOf(
                testObjectFactory.createTestProductOptionDomain(
                    fixedNow, requireNotNull(savedProductEntity.productId), "옵션 A", 10_000L
                ),
                testObjectFactory.createTestProductOptionDomain(
                    fixedNow, requireNotNull(savedProductEntity.productId), "옵션 B", 20_000L
                )
            )
            options.forEach { productOptionRepositoryAdapter.save(it) }

            // When
            val allOptions = productOptionRepositoryAdapter.findAll()

            // Then
            allOptions shouldHaveSize options.size
            allOptions.map { it.name } shouldContainAll options.map { it.name }
        }

        "존재하지 않는 ID로 조회 시 예외가 발생해야 한다" {
            // Given
            val nonExistentId: Long = 999L

            // When & Then
            val exception = shouldThrow<IllegalArgumentException> {
                productOptionRepositoryAdapter.findById(nonExistentId)
            }
            exception.message shouldBe "ProductOption with ID $nonExistentId not found"
        }

        "옵션을 ID로 삭제할 수 있어야 한다" {
            // Given
            val productEntity = testObjectFactory.createTestProductEntity(fixedNow)
            val savedProductEntity = testObjectFactory.saveProductEntity(productEntity)

            val productOption = testObjectFactory.createTestProductOptionDomain(
                fixedNow = fixedNow,
                productId = requireNotNull(savedProductEntity.productId) { "Product ID must not be null" },
                name = "옵션 테스트",
                additionalPrice = 5000L
            )
            val savedOption = productOptionRepositoryAdapter.save(productOption)

            // When
            productOptionRepositoryAdapter.deleteById(
                requireNotNull(savedOption.optionId) { "Option ID must not be null" }
            )

            // Then
            val exception = shouldThrow<IllegalArgumentException> {
                productOptionRepositoryAdapter.findById(
                    requireNotNull(savedOption.optionId) { "Option ID must not be null" }
                )
            }
            exception.message shouldBe "ProductOption with ID ${savedOption.optionId} not found"
        }
    }
}
