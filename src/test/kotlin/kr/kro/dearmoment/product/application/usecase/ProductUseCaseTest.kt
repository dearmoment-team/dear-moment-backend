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
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.dto.request.CreatePartnerShopRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.ImageReference
import kr.kro.dearmoment.product.application.dto.request.UpdatePartnerShopRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.service.ProductImageService
import kr.kro.dearmoment.product.domain.model.ConceptType
import kr.kro.dearmoment.product.domain.model.OriginalProvideType
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.domain.model.SeasonHalf
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

class ProductUseCaseTest : BehaviorSpec({

    // Mocking persistence ports 및 이미지 관련 서비스(ProductImageService)
    val productPersistencePort = mockk<ProductPersistencePort>(relaxed = true)
    val productOptionPersistencePort = mockk<ProductOptionPersistencePort>(relaxed = true)
    val productImageService = mockk<ProductImageService>(relaxed = true)
    lateinit var productUseCase: ProductUseCase

    beforeEach {
        productUseCase =
            ProductUseCaseImpl(
                productPersistencePort,
                productOptionPersistencePort,
                productImageService,
            )
    }

    afterEach {
        clearMocks(productPersistencePort, productOptionPersistencePort, productImageService)
    }

    // Helper function: Create a ProductOption instance
    fun createProductOption(
        optionId: Long = 0L,
        productId: Long = 0L,
        name: String = "Option",
        additionalPrice: Long = 0,
        description: String = "",
    ) = ProductOption(
        optionId = optionId,
        productId = productId,
        name = name,
        additionalPrice = additionalPrice,
        description = description,
    )

    // Helper function: Create a Product instance
    fun createProduct(
        productId: Long = 1L,
        userId: Long = 1L,
        title: String = "Sample Product",
        description: String = "Sample Description",
        price: Long = 10000,
        typeCode: Int = 0,
        concept: ConceptType = ConceptType.ELEGANT,
        originalProvideType: OriginalProvideType = OriginalProvideType.FULL,
        partialOriginalCount: Int? = null,
        shootingTime: LocalDateTime? = null,
        shootingLocation: String = "",
        numberOfCostumes: Int = 0,
        seasonYear: Int? = null,
        seasonHalf: SeasonHalf? = null,
        partnerShops: List<PartnerShop> = emptyList(),
        detailedInfo: String = "",
        warrantyInfo: String = "",
        contactInfo: String = "",
        options: List<ProductOption> = emptyList(),
        images: List<Image> =
            listOf(
                Image(imageId = 0L, userId = userId, fileName = "image1.jpg", url = "http://example.com/image1.jpg"),
                Image(imageId = 0L, userId = userId, fileName = "image2.jpg", url = "http://example.com/image2.jpg"),
            ),
    ) = Product(
        productId = productId,
        userId = userId,
        title = title,
        description = description,
        price = price,
        typeCode = typeCode,
        concept = concept,
        originalProvideType = originalProvideType,
        partialOriginalCount = partialOriginalCount,
        shootingTime = shootingTime,
        shootingLocation = shootingLocation,
        numberOfCostumes = numberOfCostumes,
        seasonYear = seasonYear,
        seasonHalf = seasonHalf,
        partnerShops = partnerShops,
        detailedInfo = detailedInfo,
        warrantyInfo = warrantyInfo,
        contactInfo = contactInfo,
        options = options,
        images = images,
    )

    // --- saveProduct 테스트 ---
    given("saveProduct 메소드") {
        val fixedDateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val createProductRequest =
            CreateProductRequest(
                userId = 1L,
                title = "New Product",
                description = "Product description",
                price = 10000,
                typeCode = 0,
                concept = ConceptType.ELEGANT,
                originalProvideType = OriginalProvideType.FULL,
                partialOriginalCount = null,
                shootingTime = fixedDateTime,
                shootingLocation = "Location1",
                numberOfCostumes = 3,
                seasonYear = 2023,
                seasonHalf = SeasonHalf.FIRST_HALF,
                partnerShops =
                    listOf(
                        CreatePartnerShopRequest(name = "Partner", link = "http://naver.com"),
                    ),
                detailedInfo = "Detailed product information",
                warrantyInfo = "blabla",
                contactInfo = "contact@example.com",
                options =
                    listOf(
                        CreateProductOptionRequest(name = "Option1", additionalPrice = 2000),
                    ),
            )
        val imageFile: MultipartFile =
            MockMultipartFile(
                "images",
                "image1.jpg",
                "image/jpeg",
                "fakeImageContent".toByteArray(),
            )
        // 모킹: ProductImageService.uploadImages()가 올바른 Image 리스트를 반환하도록 설정
        every { productImageService.uploadImages(listOf(imageFile), createProductRequest.userId) } returns
            listOf(
                Image(userId = createProductRequest.userId, fileName = "image1.jpg", url = "http://example.com/image1.jpg"),
            )

        // validProduct는 uploadImages()를 통해 전달받은 Image 리스트를 포함
        val validProduct =
            CreateProductRequest.toDomain(
                createProductRequest,
                listOf(Image(userId = createProductRequest.userId, fileName = "image1.jpg", url = "http://example.com/image1.jpg")),
            )

        `when`("유효한 상품 생성 요청이 오면") {
            every { productPersistencePort.existsByUserIdAndTitle(1L, "New Product") } returns false
            every { productPersistencePort.save(any()) } returns validProduct.copy(productId = 1L, userId = 1L)
            every { productOptionPersistencePort.save(any(), any()) } answers {
                firstArg<ProductOption>().copy(optionId = 1L, productId = 1L)
            }
            every { productOptionPersistencePort.findByProductId(1L) } returns
                listOf(
                    createProductOption(optionId = 1L, productId = 1L, name = "Option1", additionalPrice = 2000),
                )

            then("상품과 옵션이 저장되어야 함") {
                val result = productUseCase.saveProduct(createProductRequest, listOf(imageFile))
                result.productId shouldBe 1L
                result.options shouldHaveSize 1
                result.options[0].optionId shouldBe 1L

                // 결과 상품의 이미지 URL은 그대로 유지되어야 함.
                result.images shouldContainExactly listOf("http://example.com/image1.jpg")
                verify(exactly = 1) { productPersistencePort.existsByUserIdAndTitle(1L, "New Product") }
                verify(exactly = 1) { productPersistencePort.save(any()) }
                verify(exactly = 1) { productOptionPersistencePort.save(any(), any()) }
                verify(exactly = 1) { productOptionPersistencePort.findByProductId(1L) }
                verify(exactly = 1) { productImageService.uploadImages(listOf(imageFile), createProductRequest.userId) }
            }
        }

        `when`("중복된 제목으로 생성 요청이 오면") {
            every { productImageService.uploadImages(listOf(imageFile), createProductRequest.userId) } returns
                listOf(
                    Image(userId = createProductRequest.userId, fileName = "image1.jpg", url = "http://example.com/image1.jpg"),
                )
            every { productPersistencePort.existsByUserIdAndTitle(1L, "New Product") } returns true

            then("IllegalArgumentException 발생") {
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        productUseCase.saveProduct(createProductRequest, listOf(imageFile))
                    }
                exception.message shouldBe "A product with the same title already exists: New Product."
            }
        }
    }

    // --- updateProduct 테스트 ---
    given("updateProduct 메소드") {
        // 기존 옵션: "Old Option"과 "To Delete"
        val existingOptions =
            listOf(
                createProductOption(optionId = 1L, productId = 1L, name = "Old Option", additionalPrice = 1000),
                createProductOption(optionId = 2L, productId = 1L, name = "To Delete", additionalPrice = 2000),
            )
        // 업데이트 요청 시, 옵션 목록은 옵션 id 1은 업데이트되고, 새로운 옵션이 추가되어야 함
        // **중요:** 기존 이미지는 프론트엔드에서 URL로 전달하고, 신규 이미지는 placeholder "new_0"로 전달
        val updateProductRequest =
            UpdateProductRequest(
                productId = 1L,
                userId = 1L,
                title = "Updated Product",
                description = "Updated description",
                price = 15000,
                typeCode = 0,
                concept = ConceptType.ELEGANT,
                originalProvideType = OriginalProvideType.FULL,
                partialOriginalCount = null,
                shootingTime = null,
                shootingLocation = null,
                numberOfCostumes = null,
                seasonYear = null,
                seasonHalf = null,
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
                // 기존 이미지는 URL 형태, 신규 이미지는 placeholder "new_0" (업로드된 신규 이미지의 매핑 키)
                images =
                    listOf(
                        ImageReference("http://example.com/image1.jpg"),
                        ImageReference("new_0"),
                    ),
            )
        // 기존 제품 도메인 (업데이트 전): 기존 이미지는 "image1.jpg"와 "image2.jpg"
        val existingProductDomain =
            createProduct(
                productId = 1L,
                userId = 1L,
                title = "Original Product",
                description = "Original Description",
                price = 10000,
                options = existingOptions,
                images =
                    listOf(
                        Image(imageId = 1L, userId = 1L, fileName = "image1.jpg", url = "http://example.com/image1.jpg"),
                        Image(imageId = 1L, userId = 1L, fileName = "image2.jpg", url = "http://example.com/image2.jpg"),
                    ),
            )

        // 신규 이미지를 위한 Mock MultipartFile (실제 업로드는 ProductImageService에서 모킹)
        val newImage: MultipartFile = MockMultipartFile("images", "new_image.jpg", "image/jpeg", "newImageContent".toByteArray())

        // 모킹: 신규 이미지 업로드 관련 처리
        every { productImageService.uploadNewImagesWithPlaceholders(listOf(newImage), updateProductRequest.userId) } returns
            mapOf("new_0" to Image(userId = updateProductRequest.userId, fileName = "new_image.jpg", url = "new_image.jpg"))
        every { productImageService.resolveFinalImageOrder(any(), any(), updateProductRequest.userId) } returns
            listOf(
                Image(userId = updateProductRequest.userId, fileName = "image1.jpg", url = "http://example.com/image1.jpg"),
                Image(userId = updateProductRequest.userId, fileName = "new_image.jpg", url = "new_image.jpg"),
            )
        every { productImageService.synchronizeProductImages(any(), any(), any(), updateProductRequest.userId) } just Runs

        `when`("유효한 업데이트 요청이 오면") {
            every { productPersistencePort.findById(1L) } returns existingProductDomain
            every { productPersistencePort.save(any()) } returns
                updateProductRequest.let {
                    UpdateProductRequest.toDomain(
                        it,
                        listOf(
                            Image(userId = it.userId, fileName = "image1.jpg", url = "http://example.com/image1.jpg"),
                            Image(userId = it.userId, fileName = "new_image.jpg", url = "new_image.jpg"),
                        ),
                    ).copy(productId = 1L, userId = 1L)
                }
            // 기존 옵션 목록은 먼저 업데이트 전 옵션(existingOptions)을 반환한 후, 업데이트 후 옵션(updatedOptions)을 반환하도록 설정
            val updatedOptions =
                listOf(
                    createProductOption(optionId = 1L, productId = 1L, name = "Updated Option1", additionalPrice = 1500),
                    createProductOption(optionId = 3L, productId = 1L, name = "New Option", additionalPrice = 3000),
                )
            every { productOptionPersistencePort.findByProductId(1L) } returnsMany listOf(existingOptions, updatedOptions)
            every { productOptionPersistencePort.deleteById(2L) } just Runs
            every { productOptionPersistencePort.save(any(), any()) } answers {
                val option = firstArg<ProductOption>()
                if (option.optionId == 0L) option.copy(optionId = 3L, productId = 1L) else option
            }

            then("상품과 옵션이 업데이트되어야 하고, 이미지 동기화가 올바르게 수행되어야 함") {
                val result = productUseCase.updateProduct(updateProductRequest, listOf(newImage))
                result.productId shouldBe 1L
                result.title shouldBe "Updated Product"
                result.options shouldHaveSize 2
                result.options.map { it.name } shouldContainExactly listOf("Updated Option1", "New Option")
                result.options[0].optionId shouldBe 1L
                result.options[1].optionId shouldBe 3L

                // 최종 이미지 처리 확인:
                // 기존 이미지의 URL은 그대로 재사용("http://example.com/image1.jpg")하고,
                // 신규 이미지는 "new_image.jpg"로 처리되어야 합니다.
                result.images shouldContainExactly listOf("http://example.com/image1.jpg", "new_image.jpg")

                verify(exactly = 1) { productPersistencePort.findById(1L) }
                verify(exactly = 1) { productPersistencePort.save(any()) }
                verify(exactly = 2) { productOptionPersistencePort.findByProductId(1L) }
                verify(exactly = 1) { productOptionPersistencePort.deleteById(2L) }
                verify(exactly = 2) { productOptionPersistencePort.save(any(), any()) }
                verify(exactly = 1) { productImageService.uploadNewImagesWithPlaceholders(listOf(newImage), updateProductRequest.userId) }
                verify(exactly = 1) { productImageService.resolveFinalImageOrder(any(), any(), updateProductRequest.userId) }
                verify(exactly = 1) { productImageService.synchronizeProductImages(any(), any(), any(), updateProductRequest.userId) }
            }
        }

        `when`("존재하지 않는 상품 업데이트 요청이 오면") {
            // ★ 이미지 관련 모킹 추가: 이미지 관련 비즈니스 규칙(최소 1개 이상의 이미지 필요)을 만족시키기 위해
            every { productImageService.uploadNewImagesWithPlaceholders(listOf(newImage), updateProductRequest.userId) } returns
                mapOf("new_0" to Image(userId = updateProductRequest.userId, fileName = "new_image.jpg", url = "new_image.jpg"))
            every { productImageService.resolveFinalImageOrder(any(), any(), updateProductRequest.userId) } returns
                listOf(
                    Image(userId = updateProductRequest.userId, fileName = "image1.jpg", url = "http://example.com/image1.jpg"),
                    Image(userId = updateProductRequest.userId, fileName = "new_image.jpg", url = "new_image.jpg"),
                )
            every { productPersistencePort.findById(999L) } returns null

            then("IllegalArgumentException 발생") {
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        productUseCase.updateProduct(updateProductRequest.copy(productId = 999L), listOf(newImage))
                    }
                exception.message shouldBe "Product not found: 999"
            }
        }
    }

    // --- deleteProduct 테스트 ---
    given("deleteProduct 메소드") {
        `when`("존재하는 상품 삭제 요청이 오면") {
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

    // --- getProductById 테스트 ---
    given("getProductById 메소드") {
        `when`("존재하는 상품 조회 요청이 오면") {
            val sampleProduct =
                createProduct(
                    productId = 1L,
                    userId = 1L,
                    title = "Sample Product",
                    description = "Sample Description",
                    price = 10000,
                    options =
                        listOf(
                            createProductOption(optionId = 1L, productId = 1L, name = "Option1", additionalPrice = 2000),
                        ),
                )
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

    // --- searchProducts 테스트 ---
    given("searchProducts 메소드") {
        `when`("정렬 기준이 'likes'인 경우") {
            val sampleProducts =
                listOf(
                    createProduct(
                        productId = 1L,
                        userId = 1L,
                        title = "Product1",
                        price = 10000,
                        options = listOf(createProductOption(optionId = 1L, productId = 1L, name = "Option1", additionalPrice = 2000)),
                    ),
                    createProduct(
                        productId = 2L,
                        userId = 1L,
                        title = "Product2",
                        price = 20000,
                        options = listOf(createProductOption(optionId = 2L, productId = 2L, name = "Option2", additionalPrice = 3000)),
                    ),
                    createProduct(
                        productId = 3L,
                        userId = 1L,
                        title = "Product3",
                        price = 30000,
                        options = listOf(createProductOption(optionId = 3L, productId = 3L, name = "Option3", additionalPrice = 4000)),
                    ),
                )
            val mockProductsWithLikes =
                listOf(
                    sampleProducts[0] to 1,
                    sampleProducts[1] to 2,
                    sampleProducts[2] to 3,
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
                val results = productUseCase.searchProducts("test", 10000, 30000, sortBy = "likes", page = 0, size = 10)
                results.content shouldHaveSize 3

                val expectedOrder = mockProductsWithLikes.sortedByDescending { it.second }.map { it.first.title }
                results.content.map { it.title } shouldContainExactly expectedOrder
            }
        }

        `when`("잘못된 가격 범위로 검색 요청이 오면") {
            then("IllegalArgumentException 발생") {
                shouldThrow<IllegalArgumentException> {
                    productUseCase.searchProducts(null, 30000, 10000, page = 0, size = 10)
                }.message shouldBe "Minimum price cannot exceed maximum price."

                shouldThrow<IllegalArgumentException> {
                    productUseCase.searchProducts(null, -100, null, page = 0, size = 10)
                }.message shouldBe "Price range must be greater than or equal to 0."
            }
        }

        `when`("정렬 기준이 'price-asc'인 경우") {
            val sampleProducts =
                listOf(
                    createProduct(
                        productId = 1L,
                        userId = 1L,
                        title = "ProductA",
                        price = 10000,
                        options = listOf(createProductOption(optionId = 1L, productId = 1L, name = "OptionA", additionalPrice = 2000)),
                    ),
                    createProduct(
                        productId = 2L,
                        userId = 1L,
                        title = "ProductB",
                        price = 20000,
                        options = listOf(createProductOption(optionId = 2L, productId = 2L, name = "OptionB", additionalPrice = 3000)),
                    ),
                    createProduct(
                        productId = 3L,
                        userId = 1L,
                        title = "ProductC",
                        price = 30000,
                        options = listOf(createProductOption(optionId = 3L, productId = 3L, name = "OptionC", additionalPrice = 4000)),
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
                val results = productUseCase.searchProducts("test", 10000, 30000, sortBy = "price-asc", page = 0, size = 10)
                results.content shouldHaveSize 3

                val expectedOrder = sampleProducts.sortedBy { it.price }.map { it.title }
                results.content.map { it.title } shouldContainExactly expectedOrder
            }
        }

        `when`("정렬 기준이 'price-desc'인 경우") {
            val sampleProducts =
                listOf(
                    createProduct(
                        productId = 1L,
                        userId = 1L,
                        title = "ProductA",
                        price = 10000,
                        options = listOf(createProductOption(optionId = 1L, productId = 1L, name = "OptionA", additionalPrice = 2000)),
                    ),
                    createProduct(
                        productId = 2L,
                        userId = 1L,
                        title = "ProductB",
                        price = 20000,
                        options = listOf(createProductOption(optionId = 2L, productId = 2L, name = "OptionB", additionalPrice = 3000)),
                    ),
                    createProduct(
                        productId = 3L,
                        userId = 1L,
                        title = "ProductC",
                        price = 30000,
                        options = listOf(createProductOption(optionId = 3L, productId = 3L, name = "OptionC", additionalPrice = 4000)),
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
                val results = productUseCase.searchProducts("test", 10000, 30000, sortBy = "price-desc", page = 0, size = 10)
                results.content shouldHaveSize 3

                val expectedOrder = sampleProducts.sortedByDescending { it.price }.map { it.title }
                results.content.map { it.title } shouldContainExactly expectedOrder
            }
        }
    }
})
