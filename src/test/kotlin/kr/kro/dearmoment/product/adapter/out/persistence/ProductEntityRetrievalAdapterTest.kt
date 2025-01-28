package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.assertions.throwables.shouldThrow
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@Import(ProductEntityRetrievalAdapter::class)
@ActiveProfiles("test")
class ProductEntityRetrievalAdapterTest(
    @Autowired private val productEntityRetrievalPort: ProductEntityRetrievalPort,
    @Autowired private val jpaProductRepository: JpaProductRepository,
    @Autowired private val jpaProductOptionRepository: JpaProductOptionRepository
) : DescribeSpec({

    describe("ProductEntityRetrievalAdapter") {

        beforeEach {
            // 각 테스트 전에 데이터베이스를 초기화
            jpaProductRepository.deleteAll()
            jpaProductOptionRepository.deleteAll()
        }

        context("getProductById() 메서드는") {
            it("존재하는 ID로 조회하면 Product를 반환해야 한다") {
                val product = Product(
                    productId = 0L, // 저장 시 null로 설정될 것임
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

                val savedProductEntity = jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product))
                val savedProductId = savedProductEntity.productId!!

                val foundProduct = productEntityRetrievalPort.getProductById(savedProductId)

                foundProduct shouldNotBe null
                foundProduct?.title shouldBe "Test Product"
                foundProduct?.images?.size shouldBe 2
            }

            it("null ID로 조회하면 IllegalArgumentException을 던져야 한다") {
                shouldThrow<IllegalArgumentException> {
                    productEntityRetrievalPort.getProductById(null)
                }.message shouldBe "Product ID cannot be null"
            }

            it("존재하지 않는 ID로 조회하면 IllegalArgumentException을 던져야 한다") {
                shouldThrow<IllegalArgumentException> {
                    productEntityRetrievalPort.getProductById(999L)
                }.message shouldBe "Product with ID 999 not found"
            }

            it("옵션이 포함된 Product를 반환해야 한다") {
                val option1 = ProductOption(
                    optionId = null, // JPA expects null for new entities
                    productId = null, // will be set by ProductEntity.fromDomain
                    name = "Option 1",
                    additionalPrice = 500L,
                    description = "Option 1 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                val option2 = ProductOption(
                    optionId = null,
                    productId = null,
                    name = "Option 2",
                    additionalPrice = 1000L,
                    description = "Option 2 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                val product = Product(
                    productId = 0L,
                    userId = 1L,
                    title = "Product with Options",
                    price = 15000L,
                    typeCode = 1,
                    options = listOf(option1, option2)
                )

                val savedProductEntity = jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product))
                val savedProductId = savedProductEntity.productId!!

                val foundProduct = productEntityRetrievalPort.getProductById(savedProductId)

                foundProduct shouldNotBe null
                foundProduct?.options?.size shouldBe 2
                foundProduct?.options?.map { it.name } shouldBe listOf("Option 1", "Option 2")
                foundProduct?.options?.map { it.additionalPrice } shouldBe listOf(500L, 1000L)
                foundProduct?.options?.map { it.description } shouldBe listOf("Option 1 description", "Option 2 description")
            }
        }

        context("getAllProducts() 메서드는") {
            it("모든 Product를 반환해야 한다") {
                val product1 = Product(
                    productId = 0L,
                    userId = 1L,
                    title = "Product 1",
                    price = 10000L,
                    typeCode = 1
                )
                val product2 = Product(
                    productId = 0L,
                    userId = 2L,
                    title = "Product 2",
                    price = 20000L,
                    typeCode = 2
                )
                jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product1))
                jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product2))

                val products = productEntityRetrievalPort.getAllProducts()

                products.size shouldBe 2
                products.map { it.title } shouldBe listOf("Product 1", "Product 2")
            }

            it("데이터베이스에 Product가 없으면 빈 리스트를 반환해야 한다") {
                val products = productEntityRetrievalPort.getAllProducts()
                products.isEmpty() shouldBe true
            }
        }

        context("getProductsByUserId() 메서드는") {
            it("특정 사용자 ID에 속한 모든 Product를 반환해야 한다") {
                val product1 = Product(
                    productId = 0L,
                    userId = 1L,
                    title = "User1 Product 1",
                    price = 15000L,
                    typeCode = 1
                )
                val product2 = Product(
                    productId = 0L,
                    userId = 1L,
                    title = "User1 Product 2",
                    price = 25000L,
                    typeCode = 2
                )
                val product3 = Product(
                    productId = 0L,
                    userId = 2L,
                    title = "User2 Product",
                    price = 30000L,
                    typeCode = 3
                )
                jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product1))
                jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product2))
                jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product3))

                val user1Products = productEntityRetrievalPort.getProductsByUserId(1L)

                user1Products.size shouldBe 2
                user1Products.all { it.userId == 1L } shouldBe true
            }

            it("특정 사용자 ID에 해당하는 Product가 없으면 빈 리스트를 반환해야 한다") {
                val products = productEntityRetrievalPort.getProductsByUserId(999L)
                products.isEmpty() shouldBe true
            }
        }

        context("existsById() 메서드는") {
            it("존재하는 ID이면 true를 반환해야 한다") {
                val product = Product(
                    productId = 0L,
                    userId = 3L,
                    title = "Existence Test Product",
                    price = 5000L,
                    typeCode = 1
                )
                val savedProductEntity = jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product))
                val savedProductId = savedProductEntity.productId!!

                val exists = productEntityRetrievalPort.existsById(savedProductId)

                exists shouldBe true
            }

            it("존재하지 않는 ID이면 false를 반환해야 한다") {
                val exists = productEntityRetrievalPort.existsById(1000L)
                exists shouldBe false
            }
        }
    }
})