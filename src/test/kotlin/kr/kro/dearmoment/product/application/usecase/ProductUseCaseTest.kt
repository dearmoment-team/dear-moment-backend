package kr.kro.dearmoment.product.application.usecase

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption

class ProductUseCaseTest : BehaviorSpec({

    val productPersistencePort = mockk<ProductPersistencePort>()
    val productOptionPersistencePort = mockk<ProductOptionPersistencePort>()

    val productUseCase =
        ProductUseCase(
            productPersistencePort,
            productOptionPersistencePort,
        )

    afterEach {
        clearMocks(productPersistencePort, productOptionPersistencePort)
    }

    given("saveProduct 메소드") {
        val validProduct =
            Product(
                userId = 1L,
                title = "New Product",
                price = 10000,
                typeCode = 0,
                images = listOf("image1.jpg"),
                options = listOf(ProductOption(name = "Option1", additionalPrice = 2000)),
            )

        `when`("유효한 상품 생성 요청이 오면") {
            every { productPersistencePort.existsByUserIdAndTitle(1L, "New Product") } returns false
            every { productPersistencePort.save(validProduct) } returns validProduct.copy(productId = 1L)
            every { productOptionPersistencePort.findByProductId(1L) } returns validProduct.options
            every { productOptionPersistencePort.save(any(), any()) } answers { firstArg() }

            val result = productUseCase.saveProduct(validProduct)

            then("상품과 옵션이 저장되어야 함") {
                result.productId shouldBe 1L
                result.options shouldHaveSize 1
                verify(exactly = 1) { productPersistencePort.save(validProduct) }
                verify(exactly = 1) { productOptionPersistencePort.save(any(), any()) }
            }
        }

        `when`("중복된 제목으로 생성 요청이 오면") {
            every { productPersistencePort.existsByUserIdAndTitle(1L, "New Product") } returns true

            then("IllegalArgumentException 발생") {
                val exception =
                    io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                        productUseCase.saveProduct(validProduct)
                    }
                exception.message shouldBe "A product with the same title already exists: New Product."
            }
        }
    }

    given("updateProduct 메소드") {
        val existingOptions =
            listOf(
                ProductOption(optionId = 1L, name = "Old Option", additionalPrice = 1000),
                ProductOption(optionId = 2L, name = "To Delete", additionalPrice = 2000),
            )

        val updatedProduct =
            Product(
                productId = 1L,
                userId = 1L,
                title = "Updated Product",
                price = 15000,
                typeCode = 0,
                images = listOf("image1.jpg"),
                options =
                    listOf(
                        ProductOption(optionId = 1L, name = "Updated Option", additionalPrice = 1500),
                        ProductOption(name = "New Option", additionalPrice = 3000),
                    ),
            )

        `when`("유효한 업데이트 요청이 오면") {
            every { productPersistencePort.findById(1L) } returns
                Product(
                    productId = 1L,
                    userId = 1L,
                    title = "Original Product",
                    price = 10000,
                    typeCode = 0,
                    images = listOf("image1.jpg"),
                    options = existingOptions,
                )
            every { productPersistencePort.save(any()) } returns updatedProduct
            every { productOptionPersistencePort.findByProductId(1L) } returns updatedProduct.options
            every { productOptionPersistencePort.deleteById(2L) } just Runs
            every { productOptionPersistencePort.save(any(), any()) } answers { firstArg() }

            val result = productUseCase.updateProduct(updatedProduct)

            then("상품과 옵션이 업데이트 되어야 함") {
                result.title shouldBe "Updated Product"
                result.options.map { it.name } shouldContainExactly listOf("Updated Option", "New Option")
                verify(exactly = 1) { productOptionPersistencePort.deleteById(2L) }
                verify(exactly = 2) { productOptionPersistencePort.save(any(), any()) }
            }
        }

        `when`("존재하지 않는 상품 업데이트 요청이 오면") {
            every { productPersistencePort.findById(999L) } returns null

            then("IllegalArgumentException 발생") {
                val exception =
                    io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                        productUseCase.updateProduct(updatedProduct.copy(productId = 999L))
                    }
                exception.message shouldBe "Product with ID 999 not found."
            }
        }
    }

    given("deleteProduct 메소드") {
        `when`("존재하는 상품 삭제 요청이 오면") {
            every { productPersistencePort.existsById(1L) } returns true
            every { productOptionPersistencePort.deleteAllByProductId(1L) } just Runs
            every { productPersistencePort.deleteById(1L) } just Runs

            productUseCase.deleteProduct(1L)

            then("상품과 옵션 삭제 수행") {
                verify(exactly = 1) { productOptionPersistencePort.deleteAllByProductId(1L) }
                verify(exactly = 1) { productPersistencePort.deleteById(1L) }
            }
        }

        `when`("존재하지 않는 상품 삭제 요청이 오면") {
            every { productPersistencePort.existsById(999L) } returns false

            then("IllegalArgumentException 발생") {
                val exception =
                    io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                        productUseCase.deleteProduct(999L)
                    }
                exception.message shouldBe "The product to delete does not exist: 999."
            }
        }
    }

    given("getProductById 메소드") {
        `when`("존재하는 상품 조회 요청이 오면") {
            val sampleProduct =
                Product(
                    productId = 1L,
                    title = "Sample Product",
                    price = 10000,
                    typeCode = 0,
                    images = listOf("image1.jpg"),
                )

            every { productPersistencePort.findById(1L) } returns sampleProduct
            every { productOptionPersistencePort.findByProductId(1L) } returns
                listOf(
                    ProductOption(name = "Option1", additionalPrice = 2000),
                )

            val result = productUseCase.getProductById(1L)

            then("상품 정보와 옵션 반환") {
                result.productId shouldBe 1L
                result.options shouldHaveSize 1
            }
        }

        `when`("존재하지 않는 상품 조회 요청이 오면") {
            every { productPersistencePort.findById(999L) } returns null

            then("IllegalArgumentException 발생") {
                val exception =
                    io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                        productUseCase.getProductById(999L)
                    }
                exception.message shouldBe "Product with ID 999 not found."
            }
        }
    }

    given("searchProducts 메소드") {
        `when`("정렬 기준이 'likes'인 경우") {
            val sampleProducts =
                listOf(
                    Product(title = "Product1", price = 10000, typeCode = 0, images = listOf("img1.jpg")),
                    Product(title = "Product2", price = 20000, typeCode = 0, images = listOf("img2.jpg")),
                )

            // ✅ Mock 좋아요 데이터 추가
            val mockProductsWithLikes =
                listOf(
                    // Product1 -> 50 좋아요
                    Pair(sampleProducts[0], 50),
                    // Product2 -> 100 좋아요
                    Pair(sampleProducts[1], 100),
                )

            every {
                productPersistencePort.searchByCriteria(
                    title = "test",
                    priceRange = Pair(10000L, 30000L),
                    typeCode = null,
                    sortBy = "likes",
                )
            } returns sampleProducts

            // ✅ Mock 좋아요 개수를 고려하여 결과 생성
            val results = productUseCase.searchProducts("test", 10000, 30000, sortBy = "likes")

            then("좋아요 개수가 많은 순서로 정렬됨") {
                results.content shouldHaveSize 2

                // ✅ 좋아요가 많은 순서로 정렬 확인
                val expectedOrder = mockProductsWithLikes.sortedByDescending { it.second }.map { it.first.title }
                results.content.map { it.title } shouldContainExactly expectedOrder
            }
        }

        `when`("잘못된 가격 범위로 검색 요청이 오면") {
            then("IllegalArgumentException 발생") {
                io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    productUseCase.searchProducts(null, 30000, 10000)
                }.message shouldBe "Minimum price cannot exceed maximum price."

                io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    productUseCase.searchProducts(null, -100, null)
                }.message shouldBe "Price range must be greater than or equal to 0."
            }
        }
    }
})
