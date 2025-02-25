package kr.kro.dearmoment.product.application.usecase

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.*
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PagedResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.mock.web.MockMultipartFile

class ProductUseCaseTest : BehaviorSpec({

    // ─────────────────────────────────────────
    // Mock 선언

    val productPersistencePort = mockk<ProductPersistencePort>(relaxed = true)
    val productOptionPersistencePort = mockk<ProductOptionPersistencePort>(relaxed = true)
    val imageService = mockk<ImageService>(relaxed = true)

    // 실제 테스트 대상
    lateinit var productUseCase: ProductUseCase

    // 매번 초기화
    beforeEach {
        productUseCase = ProductUseCaseImpl(
            productPersistencePort = productPersistencePort,
            productOptionPersistencePort = productOptionPersistencePort,
            imageService = imageService,
        )
        clearMocks(productPersistencePort, productOptionPersistencePort, imageService)
    }

    // ─────────────────────────────────────────
    // 헬퍼 함수들

    fun sampleOption(
        optionId: Long = 0L,
        productId: Long = 0L,
        name: String = "Option",
        originalPrice: Long = 0,
        discountPrice: Long = 0,
        description: String = "",
    ) = ProductOption(
        optionId = optionId,
        productId = productId,
        name = name,
        optionType = kr.kro.dearmoment.product.domain.model.OptionType.SINGLE,
        discountAvailable = false,
        originalPrice = originalPrice,
        discountPrice = discountPrice,
        description = description,
        costumeCount = 1,
        shootingLocationCount = 1,
        shootingHours = 1,
        shootingMinutes = 0,
        retouchedCount = 1,
        originalProvided = false,
        partnerShops = emptyList(),
        createdAt = null,
        updatedAt = null,
    )

    fun sampleProduct(
        productId: Long = 1L,
        userId: Long = 1L,
        title: String = "Sample Product",
        description: String = "Desc",
        options: List<ProductOption> = emptyList(),
        mainImage: Image = Image(1L, userId, "main.jpg", "http://example.com/main.jpg"),
        subImages: List<Image> = emptyList(),
        additionalImages: List<Image> = emptyList(),
    ) = Product(
        productId = productId,
        userId = userId,
        productType = kr.kro.dearmoment.product.domain.model.ProductType.WEDDING_SNAP,
        shootingPlace = kr.kro.dearmoment.product.domain.model.ShootingPlace.JEJU,
        title = title,
        description = description,
        availableSeasons = emptySet(),
        cameraTypes = emptySet(),
        retouchStyles = emptySet(),
        mainImage = mainImage,
        subImages = subImages,
        additionalImages = additionalImages,
        detailedInfo = "",
        contactInfo = "",
        options = options,
    )

    // ─────────────────────────────────────────
    // saveProduct() 테스트

    given("saveProduct") {
        val req = CreateProductRequest(
            userId = 1L,
            productType = "WEDDING_SNAP",
            shootingPlace = "JEJU",
            title = "New Product",
            description = "New desc",
            // 메인 이미지(필수)
            mainImageFile = null,
            // 서브 이미지(정확히 4장이라고 가정)
            subImageFiles = emptyList(),
            // 추가 이미지(최대 5장)
            additionalImageFiles = emptyList(),
            options = listOf(
                CreateProductOptionRequest(
                    name = "Option1",
                    optionType = "SINGLE",
                    // 단품용 필수값들
                    costumeCount = 1,
                    shootingLocationCount = 1,
                    shootingHours = 1,
                    retouchedCount = 1,
                )
            )
        )

        `when`("대표 이미지가 null일 때") {
            then("IllegalArgumentException이 발생해야 함") {
                shouldThrow<IllegalArgumentException> {
                    productUseCase.saveProduct(req)
                }.message shouldBe "대표 이미지는 필수입니다."
            }
        }

        `when`("대표 이미지를 포함해 요청하면") {
            val mainImageFile = MockMultipartFile(
                "mainImageFile",
                "main.jpg",
                "image/jpeg",
                "fake-image-content".toByteArray()
            )
            val reqWithImage = req.copy(mainImageFile = mainImageFile)

            every { productPersistencePort.existsByUserIdAndTitle(1L, "New Product") } returns false
            every { imageService.save(any()) } returns 10L
            every { imageService.getOne(10L) } returns kr.kro.dearmoment.image.adapter.input.web.dto.GetImageResponse(
                imageId = 10L,
                url = "http://example.com/main-uploaded.jpg"
            )

            // ProductPort mock
            every { productPersistencePort.save(any()) } answers {
                firstArg<Product>().copy(productId = 999L)
            }
            // OptionPort mock
            every { productOptionPersistencePort.save(any(), any()) } answers {
                val optionDomain = firstArg<ProductOption>().copy(optionId = 111L, productId = 999L)
                optionDomain
            }
            every { productOptionPersistencePort.findByProductId(999L) } returns listOf(
                sampleOption(111L, 999L, "Option1")
            )

            then("이미지 업로드 후, 상품 저장 및 옵션 저장이 정상 처리됨") {
                val result = productUseCase.saveProduct(reqWithImage)

                result.productId shouldBe 999L
                result.title shouldBe "New Product"
                result.mainImage shouldBe "http://example.com/main-uploaded.jpg"
                result.options shouldHaveSize 1
                result.options[0].name shouldBe "Option1"
                result.options[0].optionId shouldBe 111L

                verify(exactly = 1) { imageService.save(any()) }  // 대표이미지 1회
                verify(exactly = 1) { imageService.getOne(10L) }
                verify(exactly = 1) { productPersistencePort.save(any()) }
                verify(exactly = 1) { productOptionPersistencePort.save(any(), any()) }
                verify(exactly = 1) { productOptionPersistencePort.findByProductId(999L) }
            }
        }
    }

    // ─────────────────────────────────────────
    // updateProduct() 테스트

    given("updateProduct") {
        val existingProduct = sampleProduct(
            productId = 1L,
            userId = 1L,
            title = "Old Product",
            description = "Old desc",
            options = listOf(
                sampleOption(optionId = 11L, productId = 1L, name = "Old Option")
            )
        )

        val req = UpdateProductRequest(
            productId = 1L,
            userId = 1L,
            productType = "WEDDING_SNAP",
            shootingPlace = "JEJU",
            title = "Updated Title",
            description = "Updated desc",
            mainImageFile = null,  // 새 대표이미지 없으면 기존 유지
            subImageFiles = null,  // 새 서브이미지 없으면 기존 유지
            additionalImageFiles = null,
            options = listOf(
                UpdateProductOptionRequest(
                    optionId = 11L,
                    name = "Updated Option",
                    optionType = "SINGLE",
                    costumeCount = 1,
                    shootingLocationCount = 1,
                    shootingHours = 1,
                    retouchedCount = 1,
                ),
                UpdateProductOptionRequest(
                    optionId = null,
                    name = "New Option",
                    optionType = "SINGLE",
                    costumeCount = 1,
                    shootingLocationCount = 1,
                    shootingHours = 1,
                    retouchedCount = 1,
                )
            )
        )

        `when`("존재하지 않는 상품 ID를 수정하려고 하면") {
            every { productPersistencePort.findById(999L) } returns null

            then("예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    productUseCase.updateProduct(req.copy(productId = 999L))
                }.message shouldBe "Product not found: 999"
            }
        }

        `when`("새 메인 이미지 없이, 기존 상품 수정 요청") {
            every { productPersistencePort.findById(1L) } returns existingProduct

            // 실제 저장 시점
            every { productPersistencePort.save(any()) } answers {
                firstArg<Product>().copy(title = "Updated Title")
            }
            // 기존 옵션 조회
            every { productOptionPersistencePort.findByProductId(1L) } returns listOf(
                sampleOption(optionId = 11L, productId = 1L, name = "Old Option")
            )
            // 삭제 대상 옵션 (예: 존재하지 않음)
            every { productOptionPersistencePort.deleteById(any()) } just Runs

            // 신규/수정 옵션
            every { productOptionPersistencePort.findById(11L) } returns sampleOption(11L, 1L, "Old Option")
            every { productOptionPersistencePort.save(any(), any()) } answers {
                val domainOption = firstArg<ProductOption>()
                // 새로 추가되는 옵션이면 optionId가 0이므로 생성된 값이라고 가정
                if (domainOption.optionId == 0L) domainOption.copy(optionId = 22L)
                else domainOption
            }

            // enrich 후 최종 옵션 목록
            every { productOptionPersistencePort.findByProductId(1L) } returns listOf(
                sampleOption(11L, 1L, "Updated Option"),
                sampleOption(22L, 1L, "New Option"),
            )

            then("기존 상품 타이틀과 옵션이 변경되고, 새 옵션 추가됨") {
                val result = productUseCase.updateProduct(req)

                result.title shouldBe "Updated Title"
                result.options shouldHaveSize 2
                result.options.map { it.name } shouldContainExactly listOf("Updated Option", "New Option")

                verify(exactly = 1) { productPersistencePort.findById(1L) }
                verify(exactly = 1) { productPersistencePort.save(any()) }
                verify(exactly = 2) { productOptionPersistencePort.findByProductId(1L) }
                verify(exactly = 1) { productOptionPersistencePort.findById(11L) }
                verify(exactly = 2) { productOptionPersistencePort.save(any(), any()) }
                verify(exactly = 0) { imageService.save(any()) } // 새 이미지 없으니 호출 없음
            }
        }

        `when`("새 대표이미지와 함께 수정 요청") {
            val newMainImage = MockMultipartFile("mainImageFile", "new_main.jpg", "image/jpeg", "fake".toByteArray())
            val reqWithImage = req.copy(mainImageFile = newMainImage)

            // 기존 상품 찾기
            every { productPersistencePort.findById(1L) } returns existingProduct

            // 이미지 업로드 mock
            every { imageService.save(any()) } returns 111L
            every {
                imageService.getOne(111L)
            } returns kr.kro.dearmoment.image.adapter.input.web.dto.GetImageResponse(
                imageId = 111L,
                url = "http://example.com/new_main.jpg"
            )

            // 상품 저장
            every { productPersistencePort.save(any()) } answers {
                firstArg<Product>().copy(title = "Updated Title With Image")
            }
            // 옵션 로직
            every { productOptionPersistencePort.findByProductId(1L) } returns listOf(
                sampleOption(11L, 1L, "Old Option")
            )
            every { productOptionPersistencePort.findById(11L) } returns sampleOption(11L, 1L, "Old Option")
            every { productOptionPersistencePort.save(any(), any()) } answers { firstArg() }
            every { productOptionPersistencePort.deleteById(any()) } just Runs
            every { productOptionPersistencePort.findByProductId(1L) } returns listOf(
                sampleOption(11L, 1L, "Updated Option"),
                sampleOption(22L, 1L, "New Option"),
            )

            then("새 대표이미지로 교체되고 상품 제목 수정 성공") {
                val result = productUseCase.updateProduct(reqWithImage)

                result.title shouldBe "Updated Title With Image"
                result.mainImage shouldBe "http://example.com/new_main.jpg"

                verify(exactly = 1) { productPersistencePort.findById(1L) }
                verify(exactly = 1) { imageService.save(any()) }
                verify(exactly = 1) { imageService.getOne(111L) }
                verify(exactly = 1) { productPersistencePort.save(any()) }
            }
        }
    }

    // ─────────────────────────────────────────
    // deleteProduct() 테스트

    given("deleteProduct") {
        val prodToDelete = sampleProduct(
            productId = 1L,
            userId = 1L,
            title = "ToDelete",
            mainImage = Image(2L, 1L, "main.jpg", "http://example.com/main.jpg"),
            subImages = listOf(
                Image(3L, 1L, "sub1.jpg", "http://example.com/sub1.jpg")
            )
        )

        `when`("존재하는 상품을 삭제") {
            every { productPersistencePort.findById(1L) } returns prodToDelete
            every { productOptionPersistencePort.deleteAllByProductId(1L) } just Runs
            every { productPersistencePort.deleteById(1L) } just Runs
            every { imageService.delete(2L) } just Runs
            every { imageService.delete(3L) } just Runs

            then("연결된 이미지, 옵션, 그리고 상품이 삭제됨") {
                productUseCase.deleteProduct(1L)

                verify(exactly = 1) { productPersistencePort.findById(1L) }
                verify(exactly = 1) { productOptionPersistencePort.deleteAllByProductId(1L) }
                verify(exactly = 1) { productPersistencePort.deleteById(1L) }
                verify(exactly = 1) { imageService.delete(2L) }
                verify(exactly = 1) { imageService.delete(3L) }
            }
        }

        `when`("존재하지 않는 상품을 삭제") {
            every { productPersistencePort.findById(999L) } returns null

            then("예외 발생") {
                val ex = shouldThrow<IllegalArgumentException> {
                    productUseCase.deleteProduct(999L)
                }
                ex.message shouldBe "The product to delete does not exist: 999."
            }
        }
    }

    // ─────────────────────────────────────────
    // getProductById() 테스트

    given("getProductById") {
        `when`("존재하는 상품 ID를 조회") {
            val sample = sampleProduct(
                productId = 77L,
                userId = 1L,
                title = "Found",
                description = "Desc",
                options = listOf(sampleOption(10L, 77L, "Option"))
            )
            every { productPersistencePort.findById(77L) } returns sample
            every { productOptionPersistencePort.findByProductId(77L) } returns sample.options

            then("상품 + 옵션을 조회하여 반환") {
                val result = productUseCase.getProductById(77L)

                result.productId shouldBe 77L
                result.title shouldBe "Found"
                result.options shouldHaveSize 1
                result.options[0].name shouldBe "Option"
            }
        }

        `when`("존재하지 않는 상품 ID") {
            every { productPersistencePort.findById(999L) } returns null
            then("예외 발생") {
                val ex = shouldThrow<IllegalArgumentException> {
                    productUseCase.getProductById(999L)
                }
                ex.message shouldBe "Product with ID 999 not found."
            }
        }
    }

    // ─────────────────────────────────────────
    // searchProducts() 테스트

    given("searchProducts") {
        `when`("특정 조건으로 검색") {
            val p1 = sampleProduct(productId = 1L, title = "P1")
            val p2 = sampleProduct(productId = 2L, title = "P2")
            every {
                productPersistencePort.searchByCriteria("title", "WEDDING_SNAP", "JEJU", "created-desc")
            } returns listOf(p1, p2)

            then("검색 결과가 페이징되어 반환됨") {
                val result: PagedResponse<ProductResponse> = productUseCase.searchProducts(
                    title = "title",
                    productType = "WEDDING_SNAP",
                    shootingPlace = "JEJU",
                    sortBy = "created-desc",
                    page = 0,
                    size = 10
                )

                result.content shouldHaveSize 2
                result.totalElements shouldBe 2
                result.totalPages shouldBe 1
            }
        }

        `when`("가격 범위 검증이 필요한 경우 (예: minPrice > maxPrice)") {
            then("코드 상 이미 제거된 기능이므로, 따로 예외 처리 X") {
                // 과거에는 minPrice, maxPrice를 쓰셨으나
                // 지금은 productType, shootingPlace 등으로 검색
                // 필요한 경우 추가 검증을 하셔도 됩니다.
            }
        }
    }

    // ─────────────────────────────────────────
    // getMainPageProducts() 테스트

    given("getMainPageProducts") {
        val p1 = sampleProduct(1L, title = "Main1")
        val p2 = sampleProduct(2L, title = "Main2")

        every { productPersistencePort.findAll() } returns listOf(p1, p2)

        `when`("메인 페이지 상품 조회") {
            then("전체 상품을 임의 정렬 후 페이징") {
                val result = productUseCase.getMainPageProducts(page = 0, size = 10)

                result.content shouldHaveSize 2
                result.content.map { it.title } shouldContainExactly listOf("Main1", "Main2")
                result.page shouldBe 0
                result.size shouldBe 10
                result.totalElements shouldBe 2
                result.totalPages shouldBe 1
            }
        }
    }
})
