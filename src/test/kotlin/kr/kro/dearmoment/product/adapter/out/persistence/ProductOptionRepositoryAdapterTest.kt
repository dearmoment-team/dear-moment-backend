package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.common.TestConfig
import kr.kro.dearmoment.product.domain.model.ProductOption
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
    private val jpaProductOptionRepository: JpaProductOptionRepository,
    private val jpaProductRepository: JpaProductRepository
) : StringSpec() {

    init {
        val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)

        "옵션을 저장하고 다시 조회할 수 있어야 한다" {
            val productEntity = createTestProductEntity(fixedNow)
            val savedProductEntity = jpaProductRepository.save(productEntity)

            val productOption = createTestProductOption(fixedNow, savedProductEntity.productId!!)
            val savedOption = productOptionRepositoryAdapter.save(productOption)

            // nullable optionId 처리
            val foundOption = productOptionRepositoryAdapter.findById(savedOption.optionId!!)
            foundOption shouldBe savedOption.copy()
        }

        "ID로 옵션을 조회할 수 있어야 한다" {
            val productEntity = createTestProductEntity(fixedNow)
            val savedProductEntity = jpaProductRepository.save(productEntity)

            val productOption = createTestProductOption(fixedNow, savedProductEntity.productId!!)
            val savedOption = productOptionRepositoryAdapter.save(productOption)

            // nullable optionId 처리
            val retrievedOption = productOptionRepositoryAdapter.findById(savedOption.optionId!!)
            retrievedOption shouldBe savedOption.copy()
        }

        "모든 옵션을 조회할 수 있어야 한다" {
            val productEntity = createTestProductEntity(fixedNow)
            val savedProductEntity = jpaProductRepository.save(productEntity)

            val options = listOf(
                createTestProductOption(fixedNow, savedProductEntity.productId!!, "옵션 A", 10_000),
                createTestProductOption(fixedNow, savedProductEntity.productId!!, "옵션 B", 20_000)
            )

            options.forEach { productOptionRepositoryAdapter.save(it) }

            val allOptions = productOptionRepositoryAdapter.findAll()

            allOptions shouldHaveSize options.size
            allOptions.map { it.name } shouldContainAll options.map { it.name }
        }

        "존재하지 않는 ID로 조회 시 예외가 발생해야 한다" {
            val nonExistentId: Long = 999L
            val exception = shouldThrow<IllegalArgumentException> {
                productOptionRepositoryAdapter.findById(nonExistentId)
            }
            exception.message shouldBe "ProductOption with ID $nonExistentId not found"
        }

        "옵션을 ID로 삭제할 수 있어야 한다" {
            val productEntity = createTestProductEntity(fixedNow)
            val savedProductEntity = jpaProductRepository.save(productEntity)

            val productOption = createTestProductOption(fixedNow, savedProductEntity.productId!!)
            val savedOption = productOptionRepositoryAdapter.save(productOption)

            // nullable optionId 처리
            productOptionRepositoryAdapter.deleteById(savedOption.optionId!!)

            val exception = shouldThrow<IllegalArgumentException> {
                productOptionRepositoryAdapter.findById(savedOption.optionId!!)
            }
            exception.message shouldBe "ProductOption with ID ${savedOption.optionId} not found"
        }
    }

    private fun createTestProductEntity(fixedNow: LocalDateTime): ProductEntity {
        return ProductEntity(
            productId = null,
            userId = 1L,
            title = "테스트 상품",
            description = "테스트 설명",
            price = 100_000,
            typeCode = 1,
            createdAt = fixedNow,
            updatedAt = fixedNow,
        )
    }

    private fun createTestProductOption(
        fixedNow: LocalDateTime,
        productId: Long,
        name: String = "테스트 옵션",
        additionalPrice: Long = 5_000L,
    ): ProductOption {
        return ProductOption(
            optionId = null,
            name = name,
            additionalPrice = additionalPrice,
            description = "$name 설명",
            productId = productId,
            createdAt = fixedNow,
            updatedAt = fixedNow,
        )
    }
}
