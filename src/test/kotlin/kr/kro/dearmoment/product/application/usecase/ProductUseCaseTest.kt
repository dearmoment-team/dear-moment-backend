package kr.kro.dearmoment.product.application.usecase

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.*
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption

class ProductUseCaseTest : BehaviorSpec({

    val productPersistencePort = mockk<ProductPersistencePort>()
    val productOptionPersistencePort = mockk<ProductOptionPersistencePort>()
    val productEntityRetrievalPort = mockk<ProductEntityRetrievalPort>()

    val productUseCase = ProductUseCase(
        productPersistencePort,
        productOptionPersistencePort,
        productEntityRetrievalPort
    )

    afterEach {
        clearMocks(productPersistencePort, productOptionPersistencePort, productEntityRetrievalPort)
    }

    given("saveProduct 메소드") {
        val validProduct = Product(
            userId = 1L,
            title = "New Product",
            price = 10000,
            typeCode = 0,
            images = listOf("image1.jpg"),
            options = listOf(ProductOption(name = "Option1", additionalPrice = 2000))
        )

        `when`("유효한 상품 생성 요청이 오면") {
            every { productPersistencePort.existsByUserIdAndTitle(1L, "New Product") } returns false
            every { productPersistencePort.save(validProduct) } returns validProduct.copy(productId = 1L)
            every { productOptionPersistencePort.findByProduct(any()) } returns validProduct.options
            every { productOptionPersistencePort.save(any()) } answers { firstArg() }

            val result = productUseCase.saveProduct(validProduct)

            then("상품과 옵션이 저장되어야 함") {
                result.productId shouldBe 1L
                result.options shouldHaveSize 1
                verify(exactly = 1) { productPersistencePort.save(validProduct) }
                verify(exactly = 1) { productOptionPersistencePort.save(any()) }
            }
        }

        `when`("중복된 제목으로 생성 요청이 오면") {
            every { productPersistencePort.existsByUserIdAndTitle(1L, "New Product") } returns true

            then("IllegalArgumentException 발생") {
                val exception = io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    productUseCase.saveProduct(validProduct)
                }
                exception.message shouldBe "A product with the same title already exists: New Product."
            }
        }
    }

    given("updateProduct 메소드") {
        val existingOptions = listOf(
            ProductOption(optionId = 1L, name = "Old Option", additionalPrice = 1000),
            ProductOption(optionId = 2L, name = "To Delete", additionalPrice = 2000)
        )

        val updatedProduct = Product(
            productId = 1L,
            userId = 1L,
            title = "Updated Product",
            price = 15000,
            typeCode = 0,
            images = listOf("image1.jpg"),
            options = listOf(
                ProductOption(optionId = 1L, name = "Updated Option", additionalPrice = 1500),
                ProductOption(name = "New Option", additionalPrice = 3000)
            )
        )

        `when`("유효한 업데이트 요청이 오면") {
            every { productEntityRetrievalPort.getProductById(1L) } returns Product(
                productId = 1L,
                userId = 1L,
                title = "Original Product",
                price = 10000,
                typeCode = 0,
                images = listOf("image1.jpg"),
                options = existingOptions
            )
            every { productPersistencePort.save(any()) } returns updatedProduct
            every { productOptionPersistencePort.findByProduct(any()) } returns updatedProduct.options
            every { productOptionPersistencePort.deleteById(2L) } just Runs
            every { productOptionPersistencePort.save(any()) } answers { firstArg() }

            val result = productUseCase.updateProduct(updatedProduct)

            then("상품과 옵션이 업데이트 되어야 함") {
                result.title shouldBe "Updated Product"
                result.options.map { it.name } shouldContainExactly listOf("Updated Option", "New Option")
                verify(exactly = 1) { productOptionPersistencePort.deleteById(2L) }
                verify(exactly = 2) { productOptionPersistencePort.save(any()) }
            }
        }

        `when`("존재하지 않는 상품 업데이트 요청이 오면") {
            every { productEntityRetrievalPort.getProductById(999L) } returns null

            then("IllegalArgumentException 발생") {
                val exception = io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    productUseCase.updateProduct(updatedProduct.copy(productId = 999L))
                }
                exception.message shouldBe "Product with ID 999 not found."
            }
        }
    }

    given("deleteProduct 메소드") {
        `when`("존재하는 상품 삭제 요청이 오면") {
            every { productEntityRetrievalPort.existsById(1L) } returns true
            every { productOptionPersistencePort.deleteAllByProductId(1L) } just Runs
            every { productPersistencePort.deleteById(1L) } just Runs

            productUseCase.deleteProduct(1L)

            then("상품과 옵션 삭제 수행") {
                verify(exactly = 1) { productOptionPersistencePort.deleteAllByProductId(1L) }
                verify(exactly = 1) { productPersistencePort.deleteById(1L) }
            }
        }

        `when`("존재하지 않는 상품 삭제 요청이 오면") {
            every { productEntityRetrievalPort.existsById(999L) } returns false

            then("IllegalArgumentException 발생") {
                val exception = io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    productUseCase.deleteProduct(999L)
                }
                exception.message shouldBe "The product to delete does not exist: 999."
            }
        }
    }

    given("getProductById 메소드") {
        `when`("존재하는 상품 조회 요청이 오면") {
            val sampleProduct = Product(
                productId = 1L,
                title = "Sample Product",
                price = 10000,
                typeCode = 0,
                images = listOf("image1.jpg")
            )

            every { productEntityRetrievalPort.getProductById(1L) } returns sampleProduct
            every { productOptionPersistencePort.findByProduct(sampleProduct) } returns listOf(
                ProductOption(name = "Option1", additionalPrice = 2000)
            )

            val result = productUseCase.getProductById(1L)

            then("상품 정보와 옵션 반환") {
                result.productId shouldBe 1L
                result.options shouldHaveSize 1
            }
        }

        `when`("존재하지 않는 상품 조회 요청이 오면") {
            every { productEntityRetrievalPort.getProductById(999L) } returns null

            then("IllegalArgumentException 발생") {
                val exception = io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    productUseCase.getProductById(999L)
                }
                exception.message shouldBe "Product with ID 999 not found."
            }
        }
    }

    given("searchProducts 메소드") {
        `when`("유효한 검색 조건으로 요청이 오면") {
            val sampleProducts = listOf(
                Product(title = "Product1", price = 10000, typeCode = 0, images = listOf("img1.jpg")),
                Product(title = "Product2", price = 20000, typeCode = 0, images = listOf("img2.jpg"))
            )

            every { productPersistencePort.searchByCriteria("test", Pair(10000L, 30000L)) } returns sampleProducts

            val results = productUseCase.searchProducts("test", 10000, 30000)

            then("조건에 맞는 상품 목록 반환") {
                results shouldHaveSize 2
                results.map { it.title } shouldContainExactly listOf("Product1", "Product2")
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

    given("Product 도메인 모델") {
        `when`("패키지 타입 상품 생성 시") {
            then("협력업체 정보 필수 검증") {
                val exception = io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    Product(
                        title = "Package Product",
                        price = 100000,
                        typeCode = 1, // 패키지 타입
                        images = listOf("image.jpg"),
                        partnerShops = emptyList() // 협력업체 정보 누락
                    )
                }
                exception.message shouldBe "패키지 상품은 하나 이상의 협력업체 정보가 필요합니다."
            }
        }

        `when`("옵션 업데이트 로직 수행 시") {
            val existing = Product(
                productId = 1L,
                userId = 1L,
                title = "Product",
                price = 10000,
                typeCode = 0,
                images = listOf("image.jpg"),
                options = listOf(
                    ProductOption(optionId = 1L, name = "Option1", additionalPrice = 1000),
                    ProductOption(optionId = 2L, name = "Option2", additionalPrice = 2000)
                )
            )

            val newOptions = listOf(
                ProductOption(optionId = 1L, name = "Updated", additionalPrice = 1500),
                ProductOption(name = "New Option", additionalPrice = 3000)
            )

            val (updated, deleted) = existing.updateOptions(newOptions)

            then("업데이트 및 삭제 대상 식별") {
                updated shouldHaveSize 2
                deleted shouldBe setOf(2L)
            }
        }

        `when`("필수 필드 검증 실패 시") {
            then("예외 발생") {
                // 이미지 누락
                io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    Product(
                        title = "No Image Product",
                        price = 10000,
                        typeCode = 0,
                        images = emptyList()
                    )
                }.message shouldBe "최소 1개 이상의 이미지가 필요합니다"

                // 제목 누락
                io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    Product(
                        title = "",
                        price = 10000,
                        typeCode = 0,
                        images = listOf("image.jpg")
                    )
                }.message shouldBe "상품명은 필수 입력값입니다"

                // 가격 음수
                io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    Product(
                        title = "Negative Price",
                        price = -1,
                        typeCode = 0,
                        images = listOf("image.jpg")
                    )
                }.message shouldBe "가격은 0 이상이어야 합니다"
            }
        }
    }

    given("ProductOption 도메인 모델") {
        `when`("유효하지 않은 옵션 생성 시") {
            then("예외 발생") {
                // 이름 누락
                io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    ProductOption(name = "", additionalPrice = 1000)
                }.message shouldBe "옵션명은 비어 있을 수 없습니다."

                // 추가 가격 음수
                io.kotest.assertions.throwables.shouldThrow<IllegalArgumentException> {
                    ProductOption(name = "Option", additionalPrice = -500)
                }.message shouldBe "추가 가격은 음수가 될 수 없습니다."
            }
        }
    }
})