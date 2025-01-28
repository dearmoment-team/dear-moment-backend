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
@ActiveProfiles("test") // application-test.properties 사용
class ProductOptionRepositoryAdapterTest(
    @Autowired private val productOptionRepositoryAdapter: ProductOptionRepositoryAdapter,
    @Autowired private val jpaProductRepository: JpaProductRepository,
    @Autowired private val jpaProductOptionRepository: JpaProductOptionRepository,
    @Autowired private val productEntityRetrievalPort: ProductEntityRetrievalPort
) : DescribeSpec({

    describe("ProductOptionRepositoryAdapter") {

        beforeEach {
            // 각 테스트 전에 데이터베이스를 초기화
            jpaProductOptionRepository.deleteAll()
            jpaProductRepository.deleteAll()
        }

        context("save() 메서드는") {
            it("유효한 ProductOption을 저장하고 반환해야 한다") {
                // 먼저 Product를 저장
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

                // 새로운 Product 객체 생성 (실제 저장된 ID 사용)
                val savedProduct = savedProductEntity.toDomain()

                // 새로운 ProductOption 생성 (optionId는 null)
                val newOption = ProductOption(
                    optionId = null,
                    productId = savedProductId,
                    name = "Option 1",
                    additionalPrice = 500L,
                    description = "Option 1 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                // 옵션 저장
                val savedOption = productOptionRepositoryAdapter.save(newOption)

                savedOption shouldNotBe null
                savedOption.optionId shouldNotBe null
                savedOption.name shouldBe "Option 1"
                savedOption.additionalPrice shouldBe 500L
                savedOption.description shouldBe "Option 1 description"
            }

            it("동일한 이름의 옵션이 이미 존재하면 예외를 던져야 한다") {
                // 먼저 Product를 저장
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

                val savedProductEntity = jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product))
                val savedProductId = savedProductEntity.productId!!

                // 새로운 Product 객체 생성 (실제 저장된 ID 사용)
                val savedProduct = savedProductEntity.toDomain()

                // 첫 번째 옵션 저장
                val option1 = ProductOption(
                    optionId = null,
                    productId = savedProductId,
                    name = "Option 1",
                    additionalPrice = 500L,
                    description = "Option 1 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                productOptionRepositoryAdapter.save(option1)

                // 동일한 이름의 옵션 저장 시도
                val duplicateOption = ProductOption(
                    optionId = null,
                    productId = savedProductId,
                    name = "Option 1",
                    additionalPrice = 700L,
                    description = "Duplicate Option",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                shouldThrow<IllegalArgumentException> {
                    productOptionRepositoryAdapter.save(duplicateOption)
                }.message shouldBe "ProductOption already exists: Option 1"
            }

            it("Product ID가 존재하지 않으면 예외를 던져야 한다") {
                val newOption = ProductOption(
                    optionId = null,
                    productId = 999L, // 존재하지 않는 Product ID
                    name = "Option 1",
                    additionalPrice = 500L,
                    description = "Option 1 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                shouldThrow<IllegalArgumentException> {
                    productOptionRepositoryAdapter.save(newOption)
                }.message shouldBe "Product with ID 999 not found"
            }
        }

        context("findById() 메서드는") {
            it("존재하는 ID로 조회하면 ProductOption을 반환해야 한다") {
                // 먼저 Product를 저장
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

                val savedProductEntity = jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product))
                val savedProductId = savedProductEntity.productId!!

                // 새로운 Product 객체 생성 (실제 저장된 ID 사용)
                val savedProduct = savedProductEntity.toDomain()

                // 옵션 저장
                val option = ProductOption(
                    optionId = null,
                    productId = savedProductId,
                    name = "Option 1",
                    additionalPrice = 500L,
                    description = "Option 1 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                val savedOption = productOptionRepositoryAdapter.save(option)

                // 옵션 조회
                val foundOption = productOptionRepositoryAdapter.findById(savedOption.optionId!!)

                foundOption shouldNotBe null
                foundOption.optionId shouldBe savedOption.optionId
                foundOption.name shouldBe "Option 1"
                foundOption.additionalPrice shouldBe 500L
                foundOption.description shouldBe "Option 1 description"
            }

            it("존재하지 않는 ID로 조회하면 예외를 던져야 한다") {
                shouldThrow<IllegalArgumentException> {
                    productOptionRepositoryAdapter.findById(999L)
                }.message shouldBe "ProductOption with ID 999 not found"
            }
        }

        context("findAll() 메서드는") {
            it("모든 ProductOption을 반환해야 한다") {
                // 먼저 Product를 저장
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

                val savedProductEntity = jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product))
                val savedProductId = savedProductEntity.productId!!

                // 새로운 Product 객체 생성 (실제 저장된 ID 사용)
                val savedProduct = savedProductEntity.toDomain()

                // 옵션 저장
                val option1 = ProductOption(
                    optionId = null,
                    productId = savedProductId,
                    name = "Option 1",
                    additionalPrice = 500L,
                    description = "Option 1 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                val option2 = ProductOption(
                    optionId = null,
                    productId = savedProductId,
                    name = "Option 2",
                    additionalPrice = 1000L,
                    description = "Option 2 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                productOptionRepositoryAdapter.save(option1)
                productOptionRepositoryAdapter.save(option2)

                // 모든 옵션 조회
                val allOptions = productOptionRepositoryAdapter.findAll()

                allOptions.size shouldBe 2
                allOptions.map { it.name } shouldBe listOf("Option 1", "Option 2")
            }

            it("데이터베이스에 ProductOption이 없으면 빈 리스트를 반환해야 한다") {
                val allOptions = productOptionRepositoryAdapter.findAll()
                allOptions.isEmpty() shouldBe true
            }
        }

        context("deleteById() 메서드는") {
            it("존재하는 ID의 ProductOption을 삭제해야 한다") {
                // 먼저 Product를 저장
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

                val savedProductEntity = jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product))
                val savedProductId = savedProductEntity.productId!!

                // 새로운 Product 객체 생성 (실제 저장된 ID 사용)
                val savedProduct = savedProductEntity.toDomain()

                // 옵션 저장
                val option = ProductOption(
                    optionId = null,
                    productId = savedProductId,
                    name = "Option 1",
                    additionalPrice = 500L,
                    description = "Option 1 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                val savedOption = productOptionRepositoryAdapter.save(option)

                // 옵션 삭제
                productOptionRepositoryAdapter.deleteById(savedOption.optionId!!)

                // 삭제 확인
                shouldThrow<IllegalArgumentException> {
                    productOptionRepositoryAdapter.findById(savedOption.optionId!!)
                }.message shouldBe "ProductOption with ID ${savedOption.optionId} not found"
            }

            it("존재하지 않는 ID의 ProductOption을 삭제하려 하면 예외를 던져야 한다") {
                shouldThrow<IllegalArgumentException> {
                    productOptionRepositoryAdapter.deleteById(999L)
                }.message shouldBe "Cannot delete ProductOption: ID 999 not found."
            }
        }

        context("findByProduct() 메서드는") {
            it("특정 Product에 속한 모든 ProductOption을 반환해야 한다") {
                // 먼저 Product를 저장
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

                val savedProductEntity = jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product))
                val savedProductId = savedProductEntity.productId!!

                // 새로운 Product 객체 생성 (실제 저장된 ID 사용)
                val savedProduct = savedProductEntity.toDomain()

                // 옵션 저장
                val option1 = ProductOption(
                    optionId = null,
                    productId = savedProductId,
                    name = "Option 1",
                    additionalPrice = 500L,
                    description = "Option 1 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                val option2 = ProductOption(
                    optionId = null,
                    productId = savedProductId,
                    name = "Option 2",
                    additionalPrice = 1000L,
                    description = "Option 2 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                productOptionRepositoryAdapter.save(option1)
                productOptionRepositoryAdapter.save(option2)

                // 특정 Product의 옵션 조회
                val productOptions = productOptionRepositoryAdapter.findByProduct(savedProduct)

                productOptions.size shouldBe 2
                productOptions.map { it.name } shouldBe listOf("Option 1", "Option 2")
            }

            it("특정 Product에 속한 옵션이 없으면 빈 리스트를 반환해야 한다") {
                // 먼저 Product를 저장
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

                val savedProductEntity = jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product))
                val savedProductId = savedProductEntity.productId!!

                // 새로운 Product 객체 생성 (실제 저장된 ID 사용)
                val savedProduct = savedProductEntity.toDomain()

                // 옵션 저장 없이 조회
                val productOptions = productOptionRepositoryAdapter.findByProduct(savedProduct)

                productOptions.isEmpty() shouldBe true
            }

            it("Product ID가 존재하지 않으면 예외를 던져야 한다") {
                val product = Product(
                    productId = null, // Product ID가 없으면 예외
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

                shouldThrow<IllegalArgumentException> {
                    productOptionRepositoryAdapter.findByProduct(product)
                }.message shouldBe "Product ID must be provided for finding options"
            }

            it("존재하지 않는 Product ID로 조회하면 예외를 던져야 한다") {
                val product = Product(
                    productId = 999L, // 존재하지 않는 Product ID
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

                shouldThrow<IllegalArgumentException> {
                    productOptionRepositoryAdapter.findByProduct(product)
                }.message shouldBe "Product with ID 999 not found"
            }
        }

        context("deleteAllByProductId() 메서드는") {
            it("특정 Product ID에 속한 모든 ProductOption을 삭제해야 한다") {
                // 먼저 Product를 저장
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

                val savedProductEntity = jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product))
                val savedProductId = savedProductEntity.productId!!

                // 새로운 Product 객체 생성 (실제 저장된 ID 사용)
                val savedProduct = savedProductEntity.toDomain()

                // 옵션 저장
                val option1 = ProductOption(
                    optionId = null,
                    productId = savedProductId,
                    name = "Option 1",
                    additionalPrice = 500L,
                    description = "Option 1 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                val option2 = ProductOption(
                    optionId = null,
                    productId = savedProductId,
                    name = "Option 2",
                    additionalPrice = 1000L,
                    description = "Option 2 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                productOptionRepositoryAdapter.save(option1)
                productOptionRepositoryAdapter.save(option2)

                // 옵션 삭제
                productOptionRepositoryAdapter.deleteAllByProductId(savedProductId)

                // 삭제 확인
                val allOptions = productOptionRepositoryAdapter.findByProduct(savedProduct)
                allOptions.isEmpty() shouldBe true
            }

            it("특정 Product ID에 속한 옵션이 없으면 아무 일도 일어나지 않아야 한다") {
                // 삭제 시도 (옵션이 없음)
                productOptionRepositoryAdapter.deleteAllByProductId(999L) // 존재하지 않는 Product ID

                // 아무 일도 일어나지 않으므로 예외가 발생하지 않아야 함
                // 추가적으로, 삭제 후 존재 여부 확인
                val exists = jpaProductOptionRepository.existsByProductProductId(999L)
                exists shouldBe false
            }
        }

        context("existsByProductId() 메서드는") {
            it("특정 Product ID에 속한 ProductOption이 존재하면 true를 반환해야 한다") {
                // 먼저 Product를 저장
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

                val savedProductEntity = jpaProductRepository.saveAndFlush(ProductEntity.fromDomain(product))
                val savedProductId = savedProductEntity.productId!!

                // 새로운 Product 객체 생성 (실제 저장된 ID 사용)
                val savedProduct = savedProductEntity.toDomain()

                // 옵션 저장
                val option = ProductOption(
                    optionId = null,
                    productId = savedProductId,
                    name = "Option 1",
                    additionalPrice = 500L,
                    description = "Option 1 description",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                productOptionRepositoryAdapter.save(option)

                // 존재 여부 확인
                val exists = productOptionRepositoryAdapter.existsByProductId(savedProductId)
                exists shouldBe true
            }

            it("특정 Product ID에 속한 ProductOption이 없으면 false를 반환해야 한다") {
                // 특정 Product ID에 속한 옵션이 없을 때
                val exists = productOptionRepositoryAdapter.existsByProductId(999L) // 존재하지 않는 Product ID
                exists shouldBe false
            }
        }
    }
})
