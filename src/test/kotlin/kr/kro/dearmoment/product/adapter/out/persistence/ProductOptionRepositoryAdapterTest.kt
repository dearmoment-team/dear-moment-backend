package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.assertions.throwables.shouldThrow
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.config.JpaConfig
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@Import(ProductOptionRepositoryAdapter::class, ProductEntityRetrievalAdapter::class, JpaConfig::class)
@ActiveProfiles("test")
class ProductOptionRepositoryAdapterTest(
    @Autowired private val productOptionRepositoryAdapter: ProductOptionRepositoryAdapter,
    @Autowired private val jpaProductRepository: JpaProductRepository,
    @Autowired private val jpaProductOptionRepository: JpaProductOptionRepository,
) : DescribeSpec({

    describe("ProductOptionRepositoryAdapter") {

        beforeEach {
            jpaProductOptionRepository.deleteAll()
            jpaProductRepository.deleteAll()
        }

        context("save() 메서드는") {
            it("유효한 ProductOption을 저장하고 반환해야 한다") {
                // Given
                val product = createTestProduct()
                val savedProductEntity = jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product))
                val newOption = createTestOption(savedProductEntity.productId!!)

                // When
                val savedOption = productOptionRepositoryAdapter.save(newOption)

                // Then
                savedOption shouldNotBe null
                savedOption.optionId shouldNotBe null
                savedOption.name shouldBe "Test Option"
                savedOption.additionalPrice shouldBe 500L
            }

            it("동일한 이름의 옵션이 이미 존재하면 예외를 던져야 한다") {
                // Given
                val product = createTestProduct()
                val savedProductEntity = jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product))
                productOptionRepositoryAdapter.save(createTestOption(savedProductEntity.productId!!))

                // When & Then
                val exception = shouldThrow<IllegalArgumentException> {
                    productOptionRepositoryAdapter.save(createTestOption(savedProductEntity.productId!!))
                }
                exception.message shouldBe "ProductOption already exists: Test Option"
            }

            it("Product ID가 존재하지 않으면 예외를 던져야 한다") {
                // Given
                val newOption = createTestOption(999L)

                // When & Then
                val exception = shouldThrow<IllegalArgumentException> {
                    productOptionRepositoryAdapter.save(newOption)
                }
                exception.message shouldBe "Product with ID 999 not found"
            }
        }
    }
})

fun createTestOption(productId: Long): ProductOption {
    return ProductOption(
        optionId = null,
        productId = productId,
        name = "Test Option",
        additionalPrice = 500L,
        description = "Test Description",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}

fun createTestProduct(): Product {
    return Product(
        productId = null,
        userId = 1L,
        title = "Test Product",
        description = "This is a test product",
        price = 10000L,
        typeCode = 1,
        shootingTime = LocalDateTime.now(),
        shootingLocation = "Seoul",
        numberOfCostumes = 2,
        packagePartnerShops = "Partner Shop A",
        detailedInfo = "Detailed information",
        warrantyInfo = "Warranty details",
        contactInfo = "contact@example.com",
        images = listOf("http://image1.com", "http://image2.com"),
        options = emptyList()
    )
}

