package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.common.TestConfig
import kr.kro.dearmoment.common.TestObjectFactory
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@DataJpaTest
@Transactional
@Import(TestConfig::class)
class ProductPersistenceAdapterTest(
    private val productPersistenceAdapter: ProductPersistenceAdapter,
    private val testObjectFactory: TestObjectFactory
) : StringSpec({

    "상품을 저장하고 올바르게 반환한다" {
        // Given
        val product = testObjectFactory.createTestProductDomain(
            userId = 123L,
            title = "Sample Product",
            description = "Sample Description",
            price = 10000L,
            typeCode = 1
        )

        // When
        val savedProduct = productPersistenceAdapter.save(product)

        // Then
        savedProduct.title shouldBe product.title
        savedProduct.price shouldBe product.price
    }

    "존재하지 않는 ID로 조회 시 null을 반환한다" {
        // When
        val result = productPersistenceAdapter.findById(999L)

        // Then
        result shouldBe null
    }

    "모든 상품을 조회하고 올바르게 반환한다" {
        // Given
        val product1 = testObjectFactory.createTestProductDomain(
            userId = 123L,
            title = "Product 1",
            description = "Description 1",
            price = 5000L,
            typeCode = 1
        )
        val product2 = testObjectFactory.createTestProductDomain(
            userId = 456L,
            title = "Product 2",
            description = "Description 2",
            price = 10000L,
            typeCode = 2
        )

        productPersistenceAdapter.save(product1)
        productPersistenceAdapter.save(product2)

        // When
        val products = productPersistenceAdapter.findAll()

        // Then
        products.size shouldBe 2
        products[0].title shouldBe "Product 1"
        products[1].title shouldBe "Product 2"
    }
})

