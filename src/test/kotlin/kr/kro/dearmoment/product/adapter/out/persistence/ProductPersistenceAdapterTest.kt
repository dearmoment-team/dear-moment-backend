package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@Import(ProductPersistenceAdapter::class)
@ActiveProfiles("test")
class ProductPersistenceAdapterTest(
    @Autowired private val productPersistencePort: ProductPersistencePort,
    @Autowired private val jpaProductRepository: JpaProductRepository,
    @Autowired private val jpaProductOptionRepository: JpaProductOptionRepository
) : DescribeSpec({

    describe("ProductPersistenceAdapter") {

        beforeEach {
            jpaProductRepository.deleteAll()
            jpaProductOptionRepository.deleteAll()
        }

        context("save() 메서드는") {
            it("Product를 저장하고 반환해야 한다") {
                val product = Product(
                    productId = 0L,
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

                val savedProduct = productPersistencePort.save(product)

                savedProduct.productId shouldNotBe 0L
                savedProduct.title shouldBe "Test Product"
                savedProduct.images.size shouldBe 2
            }
        }




        context("findByUserId() 메서드는") {
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
                productPersistencePort.save(product1)
                productPersistencePort.save(product2)
                productPersistencePort.save(product3)

                val user1Products = productPersistencePort.findByUserId(1L)

                user1Products.size shouldBe 2
                user1Products.all { it.userId == 1L } shouldBe true
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
                val savedProduct = productPersistencePort.save(product)

                // Non-null assertion 사용
                val exists = productPersistencePort.existsById(savedProduct.productId!!)

                exists shouldBe true
            }

            it("존재하지 않는 ID이면 false를 반환해야 한다") {
                val exists = productPersistencePort.existsById(1000L)
                exists shouldBe false
            }
        }

        context("searchByCriteria() 메서드는") {
            it("제목과 가격 범위에 따라 Product를 검색해야 한다") {
                val product1 = Product(
                    productId = 0L,
                    userId = 1L,
                    title = "Spring Boot Book",
                    price = 30000L,
                    typeCode = 1
                )
                val product2 = Product(
                    productId = 0L,
                    userId = 1L,
                    title = "Kotlin Programming",
                    price = 45000L,
                    typeCode = 2
                )
                val product3 = Product(
                    productId = 0L,
                    userId = 2L,
                    title = "Java Concurrency",
                    price = 35000L,
                    typeCode = 3
                )
                productPersistencePort.save(product1)
                productPersistencePort.save(product2)
                productPersistencePort.save(product3)

                val searchedProducts = productPersistencePort.searchByCriteria(
                    title = "Programming",
                    priceRange = 40000L to 50000L
                )

                searchedProducts.size shouldBe 1
                searchedProducts[0].title shouldBe "Kotlin Programming"
            }
        }

        context("deleteById() 메서드는") {
            it("특정 ID의 Product를 삭제해야 한다") {
                val product = Product(
                    productId = 0L,
                    userId = 4L,
                    title = "Delete Test Product",
                    price = 20000L,
                    typeCode = 1,
                    options = emptyList() // 옵션이 있다면 추가
                )
                val savedProduct = productPersistencePort.save(product)

                productPersistencePort.existsById(savedProduct.productId!!) shouldBe true

                productPersistencePort.deleteById(savedProduct.productId!!)

                productPersistencePort.existsById(savedProduct.productId!!) shouldBe false
            }
        }
    }
})
