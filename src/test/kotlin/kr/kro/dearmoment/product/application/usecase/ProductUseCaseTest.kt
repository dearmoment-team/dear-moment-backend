package kr.kro.dearmoment.product.application.usecase

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.*
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class ProductUseCaseTest : BehaviorSpec({

    lateinit var productOptionPersistencePort: ProductOptionPersistencePort
    lateinit var productEntityRetrievalPort: ProductEntityRetrievalPort
    lateinit var productPersistencePort: ProductPersistencePort
    lateinit var productUseCase: ProductUseCase

    beforeTest {
        // Mock 객체 초기화
        productOptionPersistencePort = mockk(relaxed = true)
        productEntityRetrievalPort = mockk(relaxed = true)
        productPersistencePort = mockk(relaxed = true)
        productUseCase = ProductUseCase(
            productPersistencePort,
            productOptionPersistencePort,
            productEntityRetrievalPort
        )
    }

    Given("ProductUseCase의 modifyProductOptions 메서드") {

        When("옵션 추가 시 기존 옵션이 없을 경우 새 옵션만 추가된다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val productEntity = ProductEntity(
                productId = 1L,
                userId = 1L,
                title = "테스트 상품",
                description = "테스트 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = mutableListOf()
            )
            val newOptions = listOf(
                ProductOption(
                    optionId = 0L,
                    productId = 1L,
                    name = "새 옵션",
                    additionalPrice = 5000,
                    description = "새 옵션 설명",
                    createdAt = fixedNow,
                    updatedAt = fixedNow,
                )
            )

            every { productEntityRetrievalPort.getProductEntityById(1L) } returns productEntity
            every { productOptionPersistencePort.findByProduct(productEntity) } returns emptyList()

            // when
            productUseCase.modifyProductOptions(1L, newOptions)

            // then
            verify(exactly = 0) { productOptionPersistencePort.deleteById(any()) }
            verify(exactly = 1) { productOptionPersistencePort.save(newOptions[0]) }
        }

        When("옵션 업데이트 시 새로운 옵션이 기존과 다르면 저장되고 기존 옵션은 삭제된다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val productEntity = ProductEntity(
                productId = 1L,
                userId = 1L,
                title = "테스트 상품",
                description = "테스트 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = mutableListOf()
            )
            val existingOptions = listOf(
                ProductOption(
                    optionId = 1L,
                    productId = 1L,
                    name = "기존 옵션",
                    additionalPrice = 1000,
                    description = "기존 옵션 설명",
                    createdAt = fixedNow,
                    updatedAt = fixedNow
                )
            )
            val newOptions = listOf(
                ProductOption(
                    optionId = 2L,
                    productId = 1L,
                    name = "새 옵션",
                    additionalPrice = 2000,
                    description = "새 옵션 설명",
                    createdAt = fixedNow,
                    updatedAt = fixedNow
                )
            )

            every { productEntityRetrievalPort.getProductEntityById(1L) } returns productEntity
            every { productOptionPersistencePort.findByProduct(productEntity) } returns existingOptions

            // when
            productUseCase.modifyProductOptions(1L, newOptions)

            // then
            verify(exactly = 1) { productOptionPersistencePort.save(newOptions[0]) }
            verify(exactly = 1) { productOptionPersistencePort.deleteById(1L) }
        }

        When("옵션 업데이트 시 중복된 옵션이 존재하면 업데이트를 적용하지 않는다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val productEntity = ProductEntity(
                productId = 1L,
                userId = 1L,
                title = "테스트 상품",
                description = "테스트 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = mutableListOf()
            )
            val existingOptions = listOf(
                ProductOption(
                    optionId = 1L,
                    productId = 1L,
                    name = "기존 옵션",
                    additionalPrice = 1000,
                    description = "기존 옵션 설명",
                    createdAt = fixedNow,
                    updatedAt = fixedNow
                )
            )
            val newOptions = listOf(
                ProductOption(
                    optionId = 1L,
                    productId = 1L,
                    name = "기존 옵션", // 동일한 옵션
                    additionalPrice = 1000,
                    description = "기존 옵션 설명",
                    createdAt = fixedNow,
                    updatedAt = fixedNow
                )
            )

            every { productEntityRetrievalPort.getProductEntityById(1L) } returns productEntity
            every { productOptionPersistencePort.findByProduct(productEntity) } returns existingOptions

            // when
            productUseCase.modifyProductOptions(1L, newOptions)

            // then
            verify(exactly = 0) { productOptionPersistencePort.save(any()) }
            verify(exactly = 0) { productOptionPersistencePort.deleteById(any()) }
        }

        When("존재하지 않는 제품 ID로 호출 시 예외를 던진다") {
            // given
            every { productEntityRetrievalPort.getProductEntityById(999L) } throws IllegalArgumentException("존재하지 않는 제품 ID입니다.")

            // when
            val exception = shouldThrow<IllegalArgumentException> {
                productUseCase.modifyProductOptions(999L, emptyList())
            }

            // then
            exception.message shouldBe "존재하지 않는 제품 ID입니다."
        }
    }

    Given("ProductUseCase의 saveProduct 메서드") {

        When("옵션 저장 실패 시 트랜잭션이 롤백된다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val product = Product(
                productId = 0L,
                userId = 1L,
                title = "새 상품",
                description = "새 상품 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = listOf(
                    ProductOption(
                        optionId = 0L,
                        productId = 0L,
                        name = "옵션 1",
                        additionalPrice = 5000,
                        description = "옵션 1 설명",
                        createdAt = fixedNow,
                        updatedAt = fixedNow
                    )
                )
            )

            every { productPersistencePort.save(product) } returns product.copy(productId = 1L)
            every { productOptionPersistencePort.save(any()) } throws RuntimeException("옵션 저장 실패")

            // when & then
            val exception = shouldThrow<RuntimeException> {
                productUseCase.saveProduct(product)
            }

            // 트랜잭션 롤백 확인
            exception.message shouldBe "옵션 저장 중 문제가 발생했습니다: 옵션 저장 실패"

            // 상품이 실제로 저장되지 않았는지 확인
            verify(exactly = 1) { productPersistencePort.save(product) }
            verify(exactly = 1) { productOptionPersistencePort.save(any()) }

            // 트랜잭션 롤백이 발생하여 데이터베이스에 저장되지 않았음을 검증
            verify(exactly = 0) { productEntityRetrievalPort.getProductEntityById(1L) }
        }

        When("상품과 옵션을 정상적으로 저장할 수 있다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val product = Product(
                productId = 0L,
                userId = 1L,
                title = "새 상품",
                description = "새 상품 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = listOf(
                    ProductOption(
                        optionId = 0L,
                        productId = 0L,
                        name = "옵션 1",
                        additionalPrice = 5000,
                        description = "옵션 1 설명",
                        createdAt = fixedNow,
                        updatedAt = fixedNow
                    )
                )
            )

            val savedProduct = product.copy(productId = 1L)

            every { productPersistencePort.save(product) } returns savedProduct
            every { productOptionPersistencePort.save(any()) } returnsArgument 0

            // when
            val result = productUseCase.saveProduct(product)

            // then
            result shouldBe savedProduct
            verify(exactly = 1) { productPersistencePort.save(product) }
            verify(exactly = 1) { productOptionPersistencePort.save(any()) }
        }

        When("옵션이 없는 상품을 저장할 수 있다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val product = Product(
                productId = 0L,
                userId = 1L,
                title = "옵션 없는 상품",
                description = "옵션 없는 상품 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = emptyList()
            )

            val savedProduct = product.copy(productId = 1L)

            every { productPersistencePort.save(product) } returns savedProduct

            // when
            val result = productUseCase.saveProduct(product)

            // then
            result shouldBe savedProduct
            verify(exactly = 1) { productPersistencePort.save(product) }
            verify(exactly = 0) { productOptionPersistencePort.save(any()) }
        }

        When("상품 저장 중 옵션 저장 실패 시 예외가 발생한다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val product = Product(
                productId = 0L,
                userId = 1L,
                title = "새 상품",
                description = "새 상품 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = listOf(
                    ProductOption(
                        optionId = 0L,
                        productId = 0L,
                        name = "옵션 1",
                        additionalPrice = 5000,
                        description = "옵션 1 설명",
                        createdAt = fixedNow,
                        updatedAt = fixedNow
                    )
                )
            )

            every { productPersistencePort.save(product) } returns product.copy(productId = 1L)
            every { productOptionPersistencePort.save(any()) } throws RuntimeException("옵션 저장 실패")

            // when & then
            val exception = shouldThrow<RuntimeException> {
                productUseCase.saveProduct(product)
            }

            exception.message shouldBe "옵션 저장 중 문제가 발생했습니다: 옵션 저장 실패"
            verify(exactly = 1) { productPersistencePort.save(product) }
            verify(exactly = 1) { productOptionPersistencePort.save(any()) }
        }


        When("상품 저장 시 상품 저장 실패 시 예외가 발생하면 옵션 저장이 시도되지 않는다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val product = Product(
                productId = 0L,
                userId = 1L,
                title = "새 상품",
                description = "새 상품 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = listOf(
                    ProductOption(
                        optionId = 0L,
                        productId = 0L,
                        name = "옵션 1",
                        additionalPrice = 5000,
                        description = "옵션 1 설명",
                        createdAt = fixedNow,
                        updatedAt = fixedNow
                    )
                )
            )

            every { productPersistencePort.save(product) } throws RuntimeException("상품 저장 실패")

            // when & then
            val exception = shouldThrow<RuntimeException> {
                productUseCase.saveProduct(product)
            }

            exception.message shouldBe "상품 저장 실패"
            verify(exactly = 1) { productPersistencePort.save(product) }
            verify(exactly = 0) { productOptionPersistencePort.save(any()) }
        }
    }

    Given("ProductUseCase의 updateProduct 메서드") {

        When("존재하는 상품을 정상적으로 업데이트할 수 있다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val existingProduct = Product(
                productId = 1L,
                userId = 1L,
                title = "기존 상품",
                description = "기존 상품 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = listOf(
                    ProductOption(
                        optionId = 1L,
                        productId = 1L,
                        name = "옵션 1",
                        additionalPrice = 5000,
                        description = "옵션 1 설명",
                        createdAt = fixedNow,
                        updatedAt = fixedNow
                    ),
                    ProductOption(
                        optionId = 2L,
                        productId = 1L,
                        name = "옵션 2",
                        additionalPrice = 7000,
                        description = "옵션 2 설명",
                        createdAt = fixedNow,
                        updatedAt = fixedNow
                    )
                )
            )

            val newOptions = listOf(
                existingProduct.options[0].copy(
                    description = "업데이트된 옵션 1 설명",
                    updatedAt = fixedNow.plusDays(1)
                ),
                ProductOption(
                    optionId = 3L,
                    productId = 1L,
                    name = "옵션 3",
                    additionalPrice = 8000,
                    description = "옵션 3 설명",
                    createdAt = fixedNow,
                    updatedAt = fixedNow
                )
            )

            val updatedProduct = existingProduct.copy(
                title = "업데이트된 상품",
                description = "업데이트된 상품 설명",
                price = 120000,
                updatedAt = fixedNow.plusDays(1),
                options = newOptions
            )

            val expectedOptions = newOptions

            // Mock 설정
            every { productPersistencePort.findById(1L) } returns existingProduct
            every { productOptionPersistencePort.findByProduct(any()) } returns existingProduct.options andThen expectedOptions
            every { productPersistencePort.save(any()) } returns updatedProduct
            every { productOptionPersistencePort.save(any()) } returnsArgument 0
            every { productEntityRetrievalPort.getProductEntityById(1L) } returns ProductEntity(
                productId = 1L,
                userId = 1L,
                title = "업데이트된 상품",
                description = "업데이트된 상품 설명",
                price = 120000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow.plusDays(1),
                options = mutableListOf()
            )

            // when
            val result = productUseCase.updateProduct(updatedProduct)

            // then
            // 개별 필드 검증
            result.productId shouldBe updatedProduct.productId
            result.title shouldBe updatedProduct.title
            result.description shouldBe updatedProduct.description
            result.price shouldBe updatedProduct.price
            result.updatedAt shouldBe updatedProduct.updatedAt
            result.options shouldHaveSize 2
            result.options[0].optionId shouldBe 1L
            result.options[0].description shouldBe "업데이트된 옵션 1 설명"
            result.options[1].optionId shouldBe 3L
            result.options[1].name shouldBe "옵션 3"

            // 검증
            verify(exactly = 1) { productPersistencePort.findById(1L) }
            verify(exactly = 1) { productOptionPersistencePort.save(newOptions[0]) }
            verify(exactly = 1) { productOptionPersistencePort.save(newOptions[1]) }
            verify(exactly = 1) { productOptionPersistencePort.deleteById(2L) }
            verify(exactly = 1) { productPersistencePort.save(any()) }
        }

        When("존재하지 않는 상품 ID로 업데이트 시 예외가 발생한다") {
            // given
            val product = Product(
                productId = 999L,
                userId = 1L,
                title = "존재하지 않는 상품",
                description = "존재하지 않는 상품 설명",
                price = 100000,
                typeCode = 1,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                options = emptyList()
            )

            every { productPersistencePort.findById(999L) } returns null

            // when & then
            val exception = shouldThrow<IllegalArgumentException> {
                productUseCase.updateProduct(product)
            }

            exception.message shouldBe "Product with ID 999 not found"
            verify(exactly = 1) { productPersistencePort.findById(999L) }
            verify(exactly = 0) { productOptionPersistencePort.save(any()) }
            verify(exactly = 0) { productPersistencePort.save(any()) }
        }

        When("옵션 업데이트 시 중복된 옵션이 존재하면 저장되지 않는다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val existingProduct = Product(
                productId = 1L,
                userId = 1L,
                title = "기존 상품",
                description = "기존 상품 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = listOf(
                    ProductOption(
                        optionId = 1L,
                        productId = 1L,
                        name = "옵션 1",
                        additionalPrice = 5000,
                        description = "옵션 1 설명",
                        createdAt = fixedNow,
                        updatedAt = fixedNow
                    )
                )
            )

            val updatedProduct = existingProduct.copy(
                options = listOf(
                    existingProduct.options[0].copy(
                        description = "업데이트된 옵션 설명"
                    ),
                    ProductOption(
                        optionId = 1L,
                        productId = 1L,
                        name = "옵션 1", // 중복된 옵션
                        additionalPrice = 5000,
                        description = "옵션 1 설명",
                        createdAt = fixedNow,
                        updatedAt = fixedNow
                    )
                )
            )

            // Mock 설정
            every { productPersistencePort.findById(1L) } returns existingProduct
            every { productOptionPersistencePort.findByProduct(any()) } returns existingProduct.options andThen updatedProduct.options
            every { productPersistencePort.save(any()) } returns updatedProduct
            every { productOptionPersistencePort.save(any()) } returnsArgument 0
            every { productEntityRetrievalPort.getProductEntityById(1L) } returns ProductEntity(
                productId = 1L,
                userId = 1L,
                title = "업데이트된 상품",
                description = "업데이트된 상품 설명",
                price = 120000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow.plusDays(1),
                options = mutableListOf()
            )

            // when
            val result = productUseCase.updateProduct(updatedProduct)

            // then
            // 개별 필드 검증
            result.productId shouldBe updatedProduct.productId
            result.title shouldBe updatedProduct.title
            result.description shouldBe updatedProduct.description
            result.price shouldBe updatedProduct.price
            result.updatedAt shouldBe updatedProduct.updatedAt
            result.options shouldHaveSize 2
            result.options[0].optionId shouldBe 1L
            result.options[0].description shouldBe "업데이트된 옵션 설명"
            result.options[1].optionId shouldBe 1L
            result.options[1].name shouldBe "옵션 1"

            // 검증
            verify(exactly = 1) { productPersistencePort.findById(1L) }
            verify(exactly = 1) { productOptionPersistencePort.save(any()) } // 업데이트된 옵션 1 설명
            verify(exactly = 0) { productOptionPersistencePort.deleteById(any()) }
            verify(exactly = 1) { productPersistencePort.save(any()) }
        }

        When("옵션 업데이트 시 일부 옵션만 삭제된다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val existingProduct = Product(
                productId = 1L,
                userId = 1L,
                title = "기존 상품",
                description = "기존 상품 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = listOf(
                    ProductOption(
                        optionId = 1L,
                        productId = 1L,
                        name = "옵션 1",
                        additionalPrice = 5000,
                        description = "옵션 1 설명",
                        createdAt = fixedNow,
                        updatedAt = fixedNow
                    ),
                    ProductOption(
                        optionId = 2L,
                        productId = 1L,
                        name = "옵션 2",
                        additionalPrice = 7000,
                        description = "옵션 2 설명",
                        createdAt = fixedNow,
                        updatedAt = fixedNow
                    )
                )
            )
            val newOptions = listOf(
                ProductOption(
                    optionId = 1L,
                    productId = 1L,
                    name = "옵션 1",
                    additionalPrice = 5000,
                    description = "업데이트된 옵션 1 설명",
                    createdAt = fixedNow,
                    updatedAt = fixedNow.plusDays(1)
                )
                // 옵션 2L은 삭제됨
            )

            val updatedProduct = Product(
                productId = 1L,
                userId = 1L,
                title = "업데이트된 상품",
                description = "업데이트된 상품 설명",
                price = 120000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow.plusDays(1),
                options = newOptions
            )

            // Mock 설정
            every { productPersistencePort.findById(1L) } returns existingProduct
            every { productOptionPersistencePort.findByProduct(any()) } returns existingProduct.options andThen newOptions
            every { productPersistencePort.save(any()) } returns updatedProduct
            every { productOptionPersistencePort.save(any()) } returnsArgument 0
            every { productEntityRetrievalPort.getProductEntityById(1L) } returns ProductEntity(
                productId = 1L,
                userId = 1L,
                title = "업데이트된 상품",
                description = "업데이트된 상품 설명",
                price = 120000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow.plusDays(1),
                options = mutableListOf()
            )

            // when
            val result = productUseCase.updateProduct(updatedProduct)

            // then
            // 개별 필드 검증
            result.productId shouldBe updatedProduct.productId
            result.title shouldBe updatedProduct.title
            result.description shouldBe updatedProduct.description
            result.price shouldBe updatedProduct.price
            result.updatedAt shouldBe updatedProduct.updatedAt
            result.options shouldHaveSize 1
            result.options[0].optionId shouldBe 1L
            result.options[0].description shouldBe "업데이트된 옵션 1 설명"

            // 검증
            verify(exactly = 1) { productPersistencePort.findById(1L) }
            verify(exactly = 1) { productOptionPersistencePort.save(newOptions[0]) }
            verify(exactly = 1) { productOptionPersistencePort.deleteById(2L) }
            verify(exactly = 1) { productPersistencePort.save(any()) }
        }
    }

    Given("ProductUseCase의 getProductById 메서드") {

        When("존재하는 상품 ID로 상품을 조회할 수 있다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val product = Product(
                productId = 1L,
                userId = 1L,
                title = "조회 테스트 상품",
                description = "조회 테스트 설명",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = listOf(
                    ProductOption(
                        optionId = 1L,
                        productId = 1L,
                        name = "옵션 1",
                        additionalPrice = 5000,
                        description = "옵션 1 설명",
                        createdAt = fixedNow,
                        updatedAt = fixedNow
                    )
                )
            )

            every { productPersistencePort.findById(1L) } returns product

            // when
            val result = productUseCase.getProductById(1L)

            // then
            result shouldBe product
            result.options shouldHaveSize 1
            result.options.first() shouldBe product.options.first()
            verify(exactly = 1) { productPersistencePort.findById(1L) }
        }

        When("존재하지 않는 상품 ID로 상품을 조회할 시 예외가 발생한다") {
            // given
            every { productPersistencePort.findById(999L) } returns null

            // when & then
            val exception = shouldThrow<IllegalArgumentException> {
                productUseCase.getProductById(999L)
            }

            exception.message shouldBe "Product with ID 999 not found"
            verify(exactly = 1) { productPersistencePort.findById(999L) }
        }
    }

})