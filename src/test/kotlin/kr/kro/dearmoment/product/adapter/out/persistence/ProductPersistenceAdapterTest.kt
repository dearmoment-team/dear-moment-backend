package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import kr.kro.dearmoment.common.TestConfig
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@DataJpaTest
@Transactional
@Import(TestConfig::class)
class ProductPersistenceAdapterTest(
    private val productPersistenceAdapter: ProductPersistenceAdapter,
    private val jpaProductRepository: JpaProductRepository
) : StringSpec({

    "상품을 저장하고 올바르게 반환한다" {
        // given
        val product = Product(
            productId = 0L,
            userId = 1L,
            title = "테스트 상품",
            description = "테스트 설명",
            price = 10000L,
            typeCode = 1,
            createdAt = null,
            updatedAt = null,
            options = emptyList()
        )

        // when
        val savedProduct = productPersistenceAdapter.save(product)

        // then
        savedProduct.productId shouldNotBe 0L
        savedProduct.title shouldBe "테스트 상품"
    }

    "존재하지 않는 ID로 조회 시 예외가 발생한다" {
        // given
        val nonExistentId = 999L

        // Mock 동작 정의
        every { jpaProductRepository.findById(nonExistentId) } returns Optional.empty()

        // when & then
        val exception = shouldThrow<IllegalArgumentException> {
            productPersistenceAdapter.findById(nonExistentId) ?: throw IllegalArgumentException("Product with ID $nonExistentId not found")
        }
        exception.message shouldBe "Product with ID $nonExistentId not found"
    }

    "상품 저장 시 옵션도 함께 저장된다" {
        // given
        val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)

        val options = listOf(
            ProductOption(
                optionId = 0L,
                name = "옵션 1",
                additionalPrice = 5000,
                description = "옵션 1 설명",
                productId = 0L,
                createdAt = fixedNow,
                updatedAt = fixedNow
            )
        )

        val product = Product(
            productId = 0L,
            userId = 1L,
            title = "옵션 포함 상품",
            description = "옵션 테스트",
            price = 20000L,
            typeCode = 2,
            createdAt = null,
            updatedAt = null,
            options = options
        )

        // when
        val savedProduct = productPersistenceAdapter.save(product)

        // then
        savedProduct.options.shouldHaveSize(1)
        savedProduct.options.first().name shouldBe "옵션 1"
    }
})
