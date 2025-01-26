package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.common.TestConfig
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDateTime

@DataJpaTest
@Import(TestConfig::class)
class ProductPersistenceAdapterTest(
    private val jpaProductRepository: JpaProductRepository
) : StringSpec({

    val productPersistenceAdapter = ProductPersistenceAdapter(jpaProductRepository)

    "상품을 저장하고 올바르게 반환한다" {
        // Given
        val product = Product(
            productId = 0L,
            userId = 123L,
            title = "Sample Product",
            description = "Sample Description",
            price = 10000L,
            typeCode = 1,
            shootingTime = LocalDateTime.now(),
            shootingLocation = "Sample Location",
            numberOfCostumes = 2,
            packagePartnerShops = "Shop1, Shop2",
            detailedInfo = "Detailed Information",
            warrantyInfo = "Warranty Information",
            contactInfo = "Contact Information",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            options = emptyList()
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
        val product1 = Product(
            productId = 0L,
            userId = 123L,
            title = "Product 1",
            description = "Description 1",
            price = 5000L,
            typeCode = 1,
            shootingTime = LocalDateTime.now(),
            shootingLocation = "Location 1",
            numberOfCostumes = 1,
            packagePartnerShops = "Shop A",
            detailedInfo = "Info A",
            warrantyInfo = "Warranty A",
            contactInfo = "Contact A",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            options = emptyList()
        )
        val product2 = Product(
            productId = 0L,
            userId = 456L,
            title = "Product 2",
            description = "Description 2",
            price = 10000L,
            typeCode = 2,
            shootingTime = LocalDateTime.now(),
            shootingLocation = "Location 2",
            numberOfCostumes = 2,
            packagePartnerShops = "Shop B",
            detailedInfo = "Info B",
            warrantyInfo = "Warranty B",
            contactInfo = "Contact B",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            options = emptyList()
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
