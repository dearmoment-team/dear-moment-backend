package kr.kro.dearmoment.product.application

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.usecase.ProductUseCase
import java.time.LocalDateTime

class ProductUseCaseTest : StringSpec({

    val productPersistencePort: ProductPersistencePort = mockk()
    val productOptionPersistencePort: ProductOptionPersistencePort = mockk()
    val productUseCase = ProductUseCase(productPersistencePort, productOptionPersistencePort)

    "상품과 옵션을 저장 후 다시 조회해본다" {
        // given
        val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
        val options = listOf(
            ProductOption(optionId = 0L, productId = 1L, name = "옵션 A", additionalPrice = 5000, description = "옵션 A 설명", createdAt = fixedNow, updatedAt = fixedNow),
            ProductOption(optionId = 0L, productId = 1L, name = "옵션 B", additionalPrice = 10000, description = "옵션 B 설명", createdAt = fixedNow, updatedAt = fixedNow)
        )
        val product = Product(
            productId = 1L,
            userId = 1L,
            title = "테스트 상품",
            description = "테스트 설명",
            price = 100000,
            typeCode = 1,
            createdAt = fixedNow,
            updatedAt = fixedNow,
            options = options
        )

        every { productPersistencePort.save(product) } returns product
        every { productOptionPersistencePort.save(options[0]) } returns options[0]
        every { productOptionPersistencePort.save(options[1]) } returns options[1]

        // when
        val savedProduct = productUseCase.saveProduct(product)

        // then
        savedProduct.shouldNotBeNull()
        savedProduct.options.shouldHaveSize(2)
    }

    "옵션을 추가하거나 삭제할 수 있다" {
        // given
        val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
        val product = Product(
            productId = 1L,
            userId = 1L,
            title = "테스트 상품",
            description = "테스트 설명",
            price = 100000,
            typeCode = 1,
            createdAt = fixedNow,
            updatedAt = fixedNow,
            options = emptyList()
        )
        val updatedOptions = listOf(
            ProductOption(optionId = 0L, productId = 1L, name = "옵션 C", additionalPrice = 15000, description = "옵션 C 설명", createdAt = fixedNow, updatedAt = fixedNow)
        )
        val updatedProduct = product.copy(options = updatedOptions)

        every { productPersistencePort.save(updatedProduct) } returns updatedProduct
        every { productOptionPersistencePort.save(updatedOptions[0]) } returns updatedOptions[0]

        // when
        val savedProduct = productUseCase.saveProduct(updatedProduct)

        // then
        savedProduct.shouldNotBeNull()
        savedProduct.options.shouldHaveSize(1)
    }

    "상품 정보를 업데이트할 수 있다" {
        // given
        val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
        val originalProduct = Product(
            productId = 1L,
            userId = 1L,
            title = "초기 상품 제목",
            description = "초기 상품 설명",
            price = 50000,
            typeCode = 1,
            createdAt = fixedNow,
            updatedAt = fixedNow,
            options = emptyList()
        )
        val updatedProduct = originalProduct.copy(
            title = "업데이트된 상품 제목",
            description = "업데이트된 상품 설명",
            price = 60000,
            updatedAt = fixedNow.plusDays(1)
        )

        every { productPersistencePort.save(updatedProduct) } returns updatedProduct

        // when
        val savedProduct = productUseCase.saveProduct(updatedProduct)

        // then
        savedProduct.shouldNotBeNull()
        savedProduct.title shouldBe "업데이트된 상품 제목"
        savedProduct.description shouldBe "업데이트된 상품 설명"
        savedProduct.price shouldBe 60000
        savedProduct.updatedAt shouldBe fixedNow.plusDays(1)
    }

    "옵션이 없는 상품을 조회할 수 있다" {
        // given
        val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
        val product = Product(
            productId = 1L,
            userId = 1L,
            title = "옵션 없는 상품",
            description = "옵션 없는 상품 설명",
            price = 50000,
            typeCode = 1,
            createdAt = fixedNow,
            updatedAt = fixedNow,
            options = emptyList()
        )

        every { productPersistencePort.findById(product.productId) } returns product

        // when
        val retrievedProduct = productUseCase.getProductById(product.productId)

        // then
        retrievedProduct.shouldNotBeNull()
        retrievedProduct.options.shouldHaveSize(0)
    }
})
