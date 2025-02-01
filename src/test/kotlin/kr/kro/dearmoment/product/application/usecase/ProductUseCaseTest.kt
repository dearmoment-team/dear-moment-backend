package kr.kro.dearmoment.product.application.usecase

import io.kotest.assertions.throwables.shouldThrow
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
import kr.kro.dearmoment.product.application.dto.extensions.toDomain
import kr.kro.dearmoment.product.application.dto.request.CreatePartnerShopRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdatePartnerShopRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class ProductUseCaseTest : BehaviorSpec({

    // 모의 객체 생성
    val productPersistencePort = mockk<ProductPersistencePort>(relaxed = true)
    val productOptionPersistencePort = mockk<ProductOptionPersistencePort>(relaxed = true)

    lateinit var productUseCase: ProductUseCase

    beforeEach {
        // 각 테스트 전에 ProductUseCase 인스턴스 초기화
        productUseCase =
            ProductUseCase(
                productPersistencePort,
                productOptionPersistencePort,
            )
    }

    afterEach {
        // 각 테스트 후에 모든 모의 객체 초기화
        clearMocks(productPersistencePort, productOptionPersistencePort)
    }

    given("saveProduct 메소드") {
        val fixedDateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val createProduct =
            CreateProductRequest(
                userId = 1L,
                title = "New Product",
                price = 10000,
                typeCode = 0,
                images = listOf("image1.jpg"),
                options = listOf(CreateProductOptionRequest(name = "Option1", additionalPrice = 2000)),
                contactInfo = "contact@example.com",
                description = "Product description",
                detailedInfo = "Detailed product information",
                numberOfCostumes = 3,
                partnerShops =
                    listOf(
                        CreatePartnerShopRequest(name = "Partner", link = "http://naver.com"),
                    ),
                shootingLocation = "Location1",
                shootingTime = fixedDateTime,
                warrantyInfo = "blabla",
            )
        // toDomain()에서는 createdAt, updatedAt이 명시적으로 할당되지 않으므로 null이 됩니다.
        val validProduct = createProduct.toDomain()

        `when`("유효한 상품 생성 요청이 오면") {
            // 모의 객체의 동작 정의
            every { productPersistencePort.existsByUserIdAndTitle(1L, "New Product") } returns false
            every { productPersistencePort.save(any()) } returns validProduct.copy(productId = 1L, userId = 1L)

            // ProductOption의 save 메소드가 호출될 때, optionId를 1L로 설정하여 반환 (Auditing 필드는 null로 남음)
            every { productOptionPersistencePort.save(any(), any()) } answers {
                firstArg<ProductOption>().copy(optionId = 1L, productId = 1L)
            }

            // findByProductId 메소드가 호출될 때, 옵션은 auditing에 의해 채워질 것이므로 null로 남은 상태를 반환 (테스트에서는 이를 그대로 사용)
            every { productOptionPersistencePort.findByProductId(1L) } returns
                listOf(
                    ProductOption(
                        optionId = 1L,
                        productId = 1L,
                        name = "Option1",
                        additionalPrice = 2000,
                        description = null,
                        createdAt = null,
                        updatedAt = null,
                    ),
                )

            then("상품과 옵션이 저장되어야 함") {
                val result = productUseCase.saveProduct(createProduct)

                result.productId shouldBe 1L
                result.options shouldHaveSize 1
                result.options[0].optionId shouldBe 1L

                verify(exactly = 1) { productPersistencePort.existsByUserIdAndTitle(1L, "New Product") }
                verify(exactly = 1) { productPersistencePort.save(any()) }
                verify(exactly = 1) { productOptionPersistencePort.save(any(), any()) }
                verify(exactly = 1) { productOptionPersistencePort.findByProductId(1L) }
            }
        }

        `when`("중복된 제목으로 생성 요청이 오면") {
            // 모의 객체의 동작 정의
            every { productPersistencePort.existsByUserIdAndTitle(1L, "New Product") } returns true

            then("IllegalArgumentException 발생") {
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        productUseCase.saveProduct(createProduct)
                    }
                exception.message shouldBe "A product with the same title already exists: New Product."
            }
        }
    }

    given("updateProduct 메소드") {
        val existingOptions =
            listOf(
                ProductOption(
                    optionId = 1L,
                    productId = 1L,
                    name = "Old Option",
                    additionalPrice = 1000,
                    createdAt = null,
                    updatedAt = null,
                ),
                ProductOption(
                    optionId = 2L,
                    productId = 1L,
                    name = "To Delete",
                    additionalPrice = 2000,
                    createdAt = null,
                    updatedAt = null,
                ),
            )

        val updateRequest =
            UpdateProductRequest(
                productId = 1L,
                title = "Updated Product",
                description = "Updated description",
                price = 15000,
                typeCode = 0,
                shootingTime = null,
                shootingLocation = null,
                numberOfCostumes = null,
                partnerShops =
                    listOf(
                        UpdatePartnerShopRequest(name = "Shop1", link = "http://shop1.com"),
                    ),
                detailedInfo = "Updated detailed info",
                warrantyInfo = "Updated warranty info",
                contactInfo = "Updated contact info",
                options =
                    listOf(
                        UpdateProductOptionRequest(
                            optionId = 1L,
                            name = "Updated Option1",
                            additionalPrice = 1500,
                            description = "Updated option1 description",
                        ),
                        UpdateProductOptionRequest(
                            optionId = null,
                            name = "New Option",
                            additionalPrice = 3000,
                            description = "New option description",
                        ),
                    ),
                images = listOf("image1.jpg"),
            )
        // userId는 NPE 방지를 위해 설정; createdAt/updatedAt은 생략
        val updatedProduct = updateRequest.toDomain().copy(userId = 1L)

        `when`("유효한 업데이트 요청이 오면") {
            // 모의 객체의 동작 정의
            every { productPersistencePort.findById(1L) } returns
                Product(
                    productId = 1L,
                    userId = 1L,
                    title = "Original Product",
                    description = "Original Description",
                    price = 10000,
                    typeCode = 0,
                    images = listOf("image1.jpg"),
                    options = existingOptions,
                    partnerShops = emptyList(),
                    detailedInfo = null,
                    warrantyInfo = null,
                    contactInfo = null,
                    createdAt = null,
                    updatedAt = null,
                )
            every { productPersistencePort.save(any()) } returns updatedProduct.copy(productId = 1L, userId = 1L)
            every { productOptionPersistencePort.findByProductId(1L) } returns
                listOf(
                    updatedProduct.options[0].copy(optionId = 1L, productId = 1L),
                    updatedProduct.options[1].copy(optionId = 3L, productId = 1L),
                )
            every { productOptionPersistencePort.deleteById(2L) } just Runs
            every { productOptionPersistencePort.save(any(), any()) } answers {
                val option = firstArg<ProductOption>()
                if (option.optionId == null) {
                    option.copy(optionId = 3L, productId = 1L)
                } else {
                    option
                }
            }

            then("상품과 옵션이 업데이트 되어야 함") {
                val result = productUseCase.updateProduct(updateRequest)

                result.productId shouldBe 1L
                result.title shouldBe "Updated Product"
                result.options shouldHaveSize 2
                result.options.map { it.name } shouldContainExactly listOf("Updated Option1", "New Option")
                result.options[0].optionId shouldBe 1L
                result.options[1].optionId shouldBe 3L

                verify(exactly = 1) { productPersistencePort.findById(1L) }
                verify(exactly = 1) { productPersistencePort.save(any()) }
                verify(exactly = 1) { productOptionPersistencePort.findByProductId(1L) }
                verify(exactly = 1) { productOptionPersistencePort.deleteById(2L) }
                verify(exactly = 2) { productOptionPersistencePort.save(any(), any()) }
            }
        }

        `when`("존재하지 않는 상품 업데이트 요청이 오면") {
            // 모의 객체의 동작 정의
            every { productPersistencePort.findById(999L) } returns null

            then("IllegalArgumentException 발생") {
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        productUseCase.updateProduct(updateRequest.copy(productId = 999L))
                    }
                exception.message shouldBe "Product not found: 999"
            }
        }
    }

    given("deleteProduct 메소드") {
        `when`("존재하는 상품 삭제 요청이 오면") {
            // 모의 객체의 동작 정의
            every { productPersistencePort.existsById(1L) } returns true
            every { productOptionPersistencePort.deleteAllByProductId(1L) } just Runs
            every { productPersistencePort.deleteById(1L) } just Runs

            then("상품과 옵션 삭제 수행") {
                productUseCase.deleteProduct(1L)

                verify(exactly = 1) { productPersistencePort.existsById(1L) }
                verify(exactly = 1) { productOptionPersistencePort.deleteAllByProductId(1L) }
                verify(exactly = 1) { productPersistencePort.deleteById(1L) }
            }
        }

        `when`("존재하지 않는 상품 삭제 요청이 오면") {
            // 모의 객체의 동작 정의
            every { productPersistencePort.existsById(999L) } returns false

            then("IllegalArgumentException 발생") {
                val exception =
                    shouldThrow<IllegalArgumentException> {
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
                    userId = 1L,
                    title = "Sample Product",
                    description = "Sample Description",
                    price = 10000,
                    typeCode = 0,
                    shootingTime = null,
                    shootingLocation = null,
                    numberOfCostumes = null,
                    partnerShops = emptyList(),
                    detailedInfo = null,
                    warrantyInfo = null,
                    contactInfo = null,
                    createdAt = null,
                    updatedAt = null,
                    options =
                        listOf(
                            ProductOption(
                                optionId = 1L,
                                productId = 1L,
                                name = "Option1",
                                additionalPrice = 2000,
                                description = null,
                                createdAt = null,
                                updatedAt = null,
                            ),
                        ),
                    images = listOf("image1.jpg"),
                )

            // 모의 객체의 동작 정의
            every { productPersistencePort.findById(1L) } returns sampleProduct
            every { productOptionPersistencePort.findByProductId(1L) } returns sampleProduct.options

            then("상품 정보와 옵션 반환") {
                val result = productUseCase.getProductById(1L)

                result.productId shouldBe 1L
                result.title shouldBe "Sample Product"
                result.options shouldHaveSize 1
                result.options[0].optionId shouldBe 1L

                verify(exactly = 1) { productPersistencePort.findById(1L) }
                verify(exactly = 1) { productOptionPersistencePort.findByProductId(1L) }
            }
        }

        `when`("존재하지 않는 상품 조회 요청이 오면") {
            // 모의 객체의 동작 정의
            every { productPersistencePort.findById(999L) } returns null

            then("IllegalArgumentException 발생") {
                val exception =
                    shouldThrow<IllegalArgumentException> {
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
                    Product(
                        productId = 1L,
                        userId = 1L,
                        title = "Product1",
                        description = "Description1",
                        price = 10000,
                        typeCode = 0,
                        shootingTime = null,
                        shootingLocation = null,
                        numberOfCostumes = null,
                        partnerShops = emptyList(),
                        detailedInfo = null,
                        warrantyInfo = null,
                        contactInfo = null,
                        createdAt = null,
                        updatedAt = null,
                        options =
                            listOf(
                                ProductOption(
                                    optionId = 1L,
                                    productId = 1L,
                                    name = "Option1",
                                    additionalPrice = 2000,
                                    createdAt = null,
                                    updatedAt = null,
                                    description = null,
                                ),
                            ),
                        images = listOf("img1.jpg"),
                    ),
                    Product(
                        productId = 2L,
                        userId = 1L,
                        title = "Product2",
                        description = "Description2",
                        price = 20000,
                        typeCode = 0,
                        shootingTime = null,
                        shootingLocation = null,
                        numberOfCostumes = null,
                        partnerShops = emptyList(),
                        detailedInfo = null,
                        warrantyInfo = null,
                        contactInfo = null,
                        createdAt = null,
                        updatedAt = null,
                        options =
                            listOf(
                                ProductOption(
                                    optionId = 2L,
                                    productId = 2L,
                                    name = "Option2",
                                    additionalPrice = 3000,
                                    createdAt = null,
                                    updatedAt = null,
                                    description = null,
                                ),
                            ),
                        images = listOf("img2.jpg"),
                    ),
                    Product(
                        productId = 3L,
                        userId = 1L,
                        title = "Product3",
                        description = "Description3",
                        price = 30000,
                        typeCode = 0,
                        shootingTime = null,
                        shootingLocation = null,
                        numberOfCostumes = null,
                        partnerShops = emptyList(),
                        detailedInfo = null,
                        warrantyInfo = null,
                        contactInfo = null,
                        createdAt = null,
                        updatedAt = null,
                        options =
                            listOf(
                                ProductOption(
                                    optionId = 3L,
                                    productId = 3L,
                                    name = "Option3",
                                    additionalPrice = 4000,
                                    createdAt = null,
                                    updatedAt = null,
                                    description = null,
                                ),
                            ),
                        images = listOf("img3.jpg"),
                    ),
                )

            // 모의 추천 수치로 인덱스+1 사용 (실제 구현에서는 추천 점수)
            val mockProductsWithLikes =
                listOf(
                    Pair(sampleProducts[0], 1),
                    Pair(sampleProducts[1], 2),
                    Pair(sampleProducts[2], 3),
                )

            every {
                productPersistencePort.searchByCriteria(
                    title = "test",
                    priceRange = Pair(10000L, 30000L),
                    typeCode = null,
                    sortBy = "likes",
                )
            } returns sampleProducts

            then("좋아요 개수가 많은 순서로 정렬됨") {
                val results = productUseCase.searchProducts("test", 10000, 30000, sortBy = "likes")

                results.content shouldHaveSize 3

                val expectedOrder = mockProductsWithLikes.sortedByDescending { it.second }.map { it.first.title }
                results.content.map { it.title } shouldContainExactly expectedOrder
            }
        }

        `when`("잘못된 가격 범위로 검색 요청이 오면") {
            then("IllegalArgumentException 발생") {
                shouldThrow<IllegalArgumentException> {
                    productUseCase.searchProducts(null, 30000, 10000)
                }.message shouldBe "Minimum price cannot exceed maximum price."

                shouldThrow<IllegalArgumentException> {
                    productUseCase.searchProducts(null, -100, null)
                }.message shouldBe "Price range must be greater than or equal to 0."
            }
        }

        `when`("정렬 기준이 'price-asc'인 경우") {
            val sampleProducts =
                listOf(
                    Product(
                        productId = 1L,
                        userId = 1L,
                        title = "ProductA",
                        description = "DescriptionA",
                        price = 10000,
                        typeCode = 0,
                        shootingTime = null,
                        shootingLocation = null,
                        numberOfCostumes = null,
                        partnerShops = emptyList(),
                        detailedInfo = null,
                        warrantyInfo = null,
                        contactInfo = null,
                        createdAt = null,
                        updatedAt = null,
                        options =
                            listOf(
                                ProductOption(
                                    optionId = 1L,
                                    productId = 1L,
                                    name = "OptionA",
                                    additionalPrice = 2000,
                                    createdAt = null,
                                    updatedAt = null,
                                    description = null,
                                ),
                            ),
                        images = listOf("imgA.jpg"),
                    ),
                    Product(
                        productId = 2L,
                        userId = 1L,
                        title = "ProductB",
                        description = "DescriptionB",
                        price = 20000,
                        typeCode = 0,
                        shootingTime = null,
                        shootingLocation = null,
                        numberOfCostumes = null,
                        partnerShops = emptyList(),
                        detailedInfo = null,
                        warrantyInfo = null,
                        contactInfo = null,
                        createdAt = null,
                        updatedAt = null,
                        options =
                            listOf(
                                ProductOption(
                                    optionId = 2L,
                                    productId = 2L,
                                    name = "OptionB",
                                    additionalPrice = 3000,
                                    createdAt = null,
                                    updatedAt = null,
                                    description = null,
                                ),
                            ),
                        images = listOf("imgB.jpg"),
                    ),
                    Product(
                        productId = 3L,
                        userId = 1L,
                        title = "ProductC",
                        description = "DescriptionC",
                        price = 30000,
                        typeCode = 0,
                        shootingTime = null,
                        shootingLocation = null,
                        numberOfCostumes = null,
                        partnerShops = emptyList(),
                        detailedInfo = null,
                        warrantyInfo = null,
                        contactInfo = null,
                        createdAt = null,
                        updatedAt = null,
                        options =
                            listOf(
                                ProductOption(
                                    optionId = 3L,
                                    productId = 3L,
                                    name = "OptionC",
                                    additionalPrice = 4000,
                                    createdAt = null,
                                    updatedAt = null,
                                    description = null,
                                ),
                            ),
                        images = listOf("imgC.jpg"),
                    ),
                )

            every {
                productPersistencePort.searchByCriteria(
                    title = "test",
                    priceRange = Pair(10000L, 30000L),
                    typeCode = null,
                    sortBy = "price-asc",
                )
            } returns sampleProducts

            then("가격 오름차순으로 정렬됨") {
                val results = productUseCase.searchProducts("test", 10000, 30000, sortBy = "price-asc")

                results.content shouldHaveSize 3

                val expectedOrder = sampleProducts.sortedBy { it.price }.map { it.title }
                results.content.map { it.title } shouldContainExactly expectedOrder
            }
        }

        `when`("정렬 기준이 'price-desc'인 경우") {
            val sampleProducts =
                listOf(
                    Product(
                        productId = 1L,
                        userId = 1L,
                        title = "ProductA",
                        description = "DescriptionA",
                        price = 10000,
                        typeCode = 0,
                        shootingTime = null,
                        shootingLocation = null,
                        numberOfCostumes = null,
                        partnerShops = emptyList(),
                        detailedInfo = null,
                        warrantyInfo = null,
                        contactInfo = null,
                        createdAt = null,
                        updatedAt = null,
                        options =
                            listOf(
                                ProductOption(
                                    optionId = 1L,
                                    productId = 1L,
                                    name = "OptionA",
                                    additionalPrice = 2000,
                                    createdAt = null,
                                    updatedAt = null,
                                    description = null,
                                ),
                            ),
                        images = listOf("imgA.jpg"),
                    ),
                    Product(
                        productId = 2L,
                        userId = 1L,
                        title = "ProductB",
                        description = "DescriptionB",
                        price = 20000,
                        typeCode = 0,
                        shootingTime = null,
                        shootingLocation = null,
                        numberOfCostumes = null,
                        partnerShops = emptyList(),
                        detailedInfo = null,
                        warrantyInfo = null,
                        contactInfo = null,
                        createdAt = null,
                        updatedAt = null,
                        options =
                            listOf(
                                ProductOption(
                                    optionId = 2L,
                                    productId = 2L,
                                    name = "OptionB",
                                    additionalPrice = 3000,
                                    createdAt = null,
                                    updatedAt = null,
                                    description = null,
                                ),
                            ),
                        images = listOf("imgB.jpg"),
                    ),
                    Product(
                        productId = 3L,
                        userId = 1L,
                        title = "ProductC",
                        description = "DescriptionC",
                        price = 30000,
                        typeCode = 0,
                        shootingTime = null,
                        shootingLocation = null,
                        numberOfCostumes = null,
                        partnerShops = emptyList(),
                        detailedInfo = null,
                        warrantyInfo = null,
                        contactInfo = null,
                        createdAt = null,
                        updatedAt = null,
                        options =
                            listOf(
                                ProductOption(
                                    optionId = 3L,
                                    productId = 3L,
                                    name = "OptionC",
                                    additionalPrice = 4000,
                                    createdAt = null,
                                    updatedAt = null,
                                    description = null,
                                ),
                            ),
                        images = listOf("imgC.jpg"),
                    ),
                )

            every {
                productPersistencePort.searchByCriteria(
                    title = "test",
                    priceRange = Pair(10000L, 30000L),
                    typeCode = null,
                    sortBy = "price-desc",
                )
            } returns sampleProducts

            then("가격 내림차순으로 정렬됨") {
                val results = productUseCase.searchProducts("test", 10000, 30000, sortBy = "price-desc")

                results.content shouldHaveSize 3

                val expectedOrder = sampleProducts.sortedByDescending { it.price }.map { it.title }
                results.content.map { it.title } shouldContainExactly expectedOrder
            }
        }
    }
})
