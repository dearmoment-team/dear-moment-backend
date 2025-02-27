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
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.*
import org.springframework.mock.web.MockMultipartFile

class ProductUseCaseTest : BehaviorSpec({

    // ─────────────────────────────────────────
    // Mock 선언
    val productPersistencePort = mockk<ProductPersistencePort>(relaxed = true)
    val productOptionPersistencePort = mockk<ProductOptionPersistencePort>(relaxed = true)
    val imageService = mockk<ImageService>(relaxed = true)

    // 실제 테스트 대상
    lateinit var productUseCase: ProductUseCase

    beforeEach {
        productUseCase = ProductUseCaseImpl(
            productPersistencePort = productPersistencePort,
            productOptionPersistencePort = productOptionPersistencePort,
            imageService = imageService
        )
        clearMocks(productPersistencePort, productOptionPersistencePort, imageService)
    }

    // ─────────────────────────────────────────
    // 헬퍼 함수 (상품/옵션 도메인)

    /**
     * 도메인 규칙: subImages는 정확히 4장
     * 아래 유틸 함수는 기본적으로 4장의 subImages를 넣는다
     */
    fun sampleProduct(
        productId: Long = 1L,
        userId: Long = 1L,
        title: String = "Sample Product",
        description: String = "Desc",
        mainImageId: Long = 100L,
        subImageIds: List<Long> = listOf(101L, 102L, 103L, 104L),
        options: List<ProductOption> = emptyList(),
    ): Product {
        val mainImg = Image(
            imageId = mainImageId,
            userId = userId,
            fileName = "main.jpg",
            url = "http://example.com/main.jpg"
        )
        val subImgs = subImageIds.mapIndexed { idx, id ->
            Image(
                imageId = id,
                userId = userId,
                fileName = "sub$idx.jpg",
                url = "http://example.com/sub$idx.jpg"
            )
        }
        return Product(
            productId = productId,
            userId = userId,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = title,
            description = description,
            availableSeasons = emptySet(),
            cameraTypes = emptySet(),
            retouchStyles = emptySet(),
            mainImage = mainImg,
            subImages = subImgs, // 4장
            additionalImages = emptyList(),
            detailedInfo = "",
            contactInfo = "",
            options = options,
        )
    }

    fun sampleOption(
        optionId: Long = 0L,
        productId: Long = 0L,
        name: String = "Option"
    ) = ProductOption(
        optionId = optionId,
        productId = productId,
        name = name,
        optionType = OptionType.SINGLE,
        discountAvailable = false,
        originalPrice = 1000,
        discountPrice = 0,
        description = "desc",
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

    // ─────────────────────────────────────────
    // saveProduct() 테스트

    given("saveProduct") {

        // 서브 이미지 4장 준비
        val sub1 = MockMultipartFile("sub1", "sub1.jpg", "image/jpeg", "fake-sub1".toByteArray())
        val sub2 = MockMultipartFile("sub2", "sub2.jpg", "image/jpeg", "fake-sub2".toByteArray())
        val sub3 = MockMultipartFile("sub3", "sub3.jpg", "image/jpeg", "fake-sub3".toByteArray())
        val sub4 = MockMultipartFile("sub4", "sub4.jpg", "image/jpeg", "fake-sub4".toByteArray())

        val createReq = CreateProductRequest(
            userId = 1L,
            productType = "WEDDING_SNAP",
            shootingPlace = "JEJU",
            title = "New Product",
            description = "New desc",
            mainImageFile = null,  // 대표 이미지 X -> 예외
            subImageFiles = listOf(sub1, sub2, sub3, sub4),
            additionalImageFiles = emptyList(),
            options = listOf(
                CreateProductOptionRequest(
                    name = "Option1",
                    optionType = "SINGLE",
                    costumeCount = 1,
                    shootingLocationCount = 1,
                    shootingHours = 1,
                    retouchedCount = 1,
                )
            )
        )

        `when`("대표 이미지가 null이면") {
            then("예외 발생: 대표 이미지는 필수입니다.") {
                val ex = shouldThrow<IllegalArgumentException> {
                    productUseCase.saveProduct(createReq)
                }
                ex.message shouldBe "대표 이미지는 필수입니다."
            }
        }

        `when`("대표 이미지를 포함해 정상 요청") {
            val mainFile = MockMultipartFile("main", "main.jpg", "image/jpeg", "fake-main".toByteArray())
            val reqWithMain = createReq.copy(mainImageFile = mainFile)

            // Mock: 같은 제목 상품 존재 여부
            every { productPersistencePort.existsByUserIdAndTitle(1L, "New Product") } returns false

            // Mock: 이미지 업로드 (대표 1 + 서브4 => 총 5회)
            every { imageService.save(any()) } returnsMany listOf(10L, 11L, 12L, 13L, 14L)
            // 대표 이미지 getOne(10L):
            every { imageService.getOne(10L) } returns
                    kr.kro.dearmoment.image.adapter.input.web.dto.GetImageResponse(
                        imageId = 10L,
                        url = "http://example.com/main-uploaded.jpg"
                    )

            // Mock: DB 저장 시 productId=999
            val savedDomain = sampleProduct(
                productId = 999L,
                title = "New Product"
            )
            every { productPersistencePort.save(any()) } returns savedDomain

            // Mock: 옵션 저장 => optionId=111
            every { productOptionPersistencePort.save(any(), any()) } answers {
                firstArg<ProductOption>().copy(optionId = 111L, productId = 999L)
            }
            every { productOptionPersistencePort.findByProductId(999L) } returns listOf(
                sampleOption(optionId = 111L, productId = 999L, name = "Option1")
            )

            then("상품 & 옵션 저장, 대표+서브 이미지 업로드 확인") {
                val result = productUseCase.saveProduct(reqWithMain)

                result.productId shouldBe 999L
                result.title shouldBe "New Product"
                result.mainImage shouldBe "http://example.com/main-uploaded.jpg"

                // 옵션 확인
                result.options shouldHaveSize 1
                result.options[0].optionId shouldBe 111L
                result.options[0].name shouldBe "Option1"

                // 대표+서브4장 => 5회 호출
                verify(exactly = 5) { imageService.save(any()) }
                // 대표이미지에 대한 getOne(10L) 확인
                verify(exactly = 1) { imageService.getOne(10L) }
                // 상품, 옵션 저장
                verify(exactly = 1) { productPersistencePort.save(any()) }
                verify(exactly = 1) { productOptionPersistencePort.save(any(), any()) }
                verify(exactly = 1) { productOptionPersistencePort.findByProductId(999L) }
            }
        }
    }

    // ─────────────────────────────────────────
    // updateProduct() 테스트

    given("updateProduct") {
        // 기존 상품(1L), main=10L, sub=11..14 => 도메인 규칙 충족
        val existingProduct = sampleProduct(
            productId = 1L,
            title = "Old Product",
            mainImageId = 10L,
            subImageIds = listOf(11L, 12L, 13L, 14L),
            options = listOf(sampleOption(optionId = 100L, productId = 1L, name = "Old Option"))
        )

        val updateReq = UpdateProductRequest(
            productId = 1L,
            userId = 1L,
            productType = "WEDDING_SNAP",
            shootingPlace = "JEJU",
            title = "Updated Title",
            description = "Updated desc",
            mainImageFile = null,   // 새 대표이미지 X
            subImageFiles = null,   // 새 서브이미지 X => 기존 11..14 유지
            additionalImageFiles = null,
            options = listOf(
                UpdateProductOptionRequest(
                    optionId = 100L,
                    name = "Updated Option",
                    optionType = "SINGLE",
                    costumeCount = 1,
                    shootingLocationCount = 1,
                    shootingHours = 1,
                    shootingMinutes = 0,
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

        `when`("존재하지 않는 상품 ID") {
            every { productPersistencePort.findById(999L) } returns null

            then("예외 발생") {
                val ex = shouldThrow<IllegalArgumentException> {
                    productUseCase.updateProduct(updateReq.copy(productId = 999L))
                }
                ex.message shouldBe "Product not found: 999"
            }
        }

        `when`("새 대표이미지 없이 기존 상품 수정") {
            // Mock: 기존 상품 조회
            every { productPersistencePort.findById(1L) } returns existingProduct

            // Mock: 저장 => title 변경
            every { productPersistencePort.save(any()) } answers {
                firstArg<Product>().copy(title = "Updated Title")
            }

            // Mock: 기존 옵션 리스트
            every { productOptionPersistencePort.findByProductId(1L) } returnsMany listOf(
                // 처음 조회 시: 구 옵션 1개
                listOf(sampleOption(optionId = 100L, productId = 1L, name = "Old Option")),
                // 두 번째 조회 시(마지막 enrich 단계): 업데이트된 2개
                listOf(
                    sampleOption(optionId = 100L, productId = 1L, name = "Updated Option"),
                    sampleOption(optionId = 200L, productId = 1L, name = "New Option"),
                )
            )

            every { productOptionPersistencePort.deleteById(any()) } just Runs

            // Mock: 기존 옵션 개별 조회
            every { productOptionPersistencePort.findById(100L) } returns
                    sampleOption(100L, 1L, "Old Option")

            // Mock: 옵션 저장
            every { productOptionPersistencePort.save(any(), any()) } answers {
                val argOption = firstArg<ProductOption>()
                // 새로 추가된 옵션이면 ID=0 → ID=200으로 가정
                if (argOption.optionId == 0L) argOption.copy(optionId = 200L) else argOption
            }

            then("타이틀=Updated Title, 기존옵션->Updated Option, 새옵션->New Option") {
                val result = productUseCase.updateProduct(updateReq)

                result.title shouldBe "Updated Title"
                // 최종 옵션 2개
                result.options shouldHaveSize 2
                result.options.map { it.name } shouldContainExactly listOf("Updated Option", "New Option")

                verify(exactly = 1) { productPersistencePort.findById(1L) }
                verify(exactly = 1) { productPersistencePort.save(any()) }

                // 옵션 조회 2번(기존, 최종)
                verify(exactly = 2) { productOptionPersistencePort.findByProductId(1L) }
                // 구 옵션 조회
                verify(exactly = 1) { productOptionPersistencePort.findById(100L) }
                // save 2회(수정1, 신규1)
                verify(exactly = 2) { productOptionPersistencePort.save(any(), any()) }

                // 대표이미지 교체 없음 => imageService.save(...) 호출 0회
                verify(exactly = 0) { imageService.save(any()) }
            }
        }

        `when`("새 대표이미지를 포함해 수정") {
            val newMain = MockMultipartFile("main", "new_main.jpg", "image/jpeg", "fake".toByteArray())
            val reqWithNewMain = updateReq.copy(mainImageFile = newMain)

            // Mock: 기존 상품
            every { productPersistencePort.findById(1L) } returns existingProduct
            // Mock: 새 대표이미지 업로드 => ID=777
            every { imageService.save(any()) } returns 777L
            every {
                imageService.getOne(777L)
            } returns kr.kro.dearmoment.image.adapter.input.web.dto.GetImageResponse(
                imageId = 777L,
                url = "http://example.com/new_main.jpg"
            )

            // Mock: 상품 저장 => title="Updated Title With NewMain"
            every { productPersistencePort.save(any()) } answers {
                firstArg<Product>().copy(title = "Updated Title With NewMain")
            }

            // Mock: 옵션 로직
            every { productOptionPersistencePort.findByProductId(1L) } returnsMany listOf(
                listOf(sampleOption(optionId = 100L, productId = 1L, name = "Old Option")),
                listOf(
                    sampleOption(optionId = 100L, productId = 1L, name = "Updated Option"),
                    sampleOption(optionId = 200L, productId = 1L, name = "New Option"),
                )
            )
            every { productOptionPersistencePort.findById(100L) } returns
                    sampleOption(100L, 1L, "Old Option")
            every { productOptionPersistencePort.save(any(), any()) } returns
                    sampleOption(100L, 1L, "Updated Option")

            then("대표이미지 교체 & 옵션 수정") {
                val result = productUseCase.updateProduct(reqWithNewMain)

                result.title shouldBe "Updated Title With NewMain"
                result.mainImage shouldBe "http://example.com/new_main.jpg"

                verify(exactly = 1) { productPersistencePort.findById(1L) }
                verify(exactly = 1) { imageService.save(any()) }
                verify(exactly = 1) { imageService.getOne(777L) }
                verify(exactly = 1) { productPersistencePort.save(any()) }
            }
        }
    }

    // ─────────────────────────────────────────
    // deleteProduct() 테스트

    given("deleteProduct") {
        // 기존 상품: 대표=2, 서브=3..6 (4장)
        val prodToDelete = sampleProduct(
            productId = 1L,
            mainImageId = 2L,
            subImageIds = listOf(3L, 4L, 5L, 6L)
        )

        `when`("존재하는 상품 삭제") {
            every { productPersistencePort.findById(1L) } returns prodToDelete
            every { productOptionPersistencePort.deleteAllByProductId(1L) } just Runs
            every { productPersistencePort.deleteById(1L) } just Runs

            // 이미지 삭제: 대표=2, 서브=3..6
            every { imageService.delete(2L) } just Runs
            every { imageService.delete(3L) } just Runs
            every { imageService.delete(4L) } just Runs
            every { imageService.delete(5L) } just Runs
            every { imageService.delete(6L) } just Runs

            then("상품, 옵션, 이미지 삭제") {
                productUseCase.deleteProduct(1L)

                verify(exactly = 1) { productPersistencePort.findById(1L) }
                verify(exactly = 1) { productOptionPersistencePort.deleteAllByProductId(1L) }
                verify(exactly = 1) { productPersistencePort.deleteById(1L) }

                // 대표+서브(4장)=총5장
                verify(exactly = 1) { imageService.delete(2L) }
                verify(exactly = 1) { imageService.delete(3L) }
                verify(exactly = 1) { imageService.delete(4L) }
                verify(exactly = 1) { imageService.delete(5L) }
                verify(exactly = 1) { imageService.delete(6L) }
            }
        }

        `when`("존재하지 않는 상품 삭제") {
            every { productPersistencePort.findById(999L) } returns null

            then("예외") {
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
        `when`("존재하는 상품 ID=77L") {
            // mock된 상품, productId=77, subImages=111..114
            val p = sampleProduct(
                productId = 77L,
                mainImageId = 110L,
                subImageIds = listOf(111L, 112L, 113L, 114L),
                options = listOf(sampleOption(optionId = 10L, productId = 77L, name = "Option"))
            )
            every { productPersistencePort.findById(77L) } returns p
            every { productOptionPersistencePort.findByProductId(77L) } returns p.options

            then("Product + Option 반환") {
                val result = productUseCase.getProductById(77L)
                result.productId shouldBe 77L
                result.title shouldBe "Sample Product"
                result.options shouldHaveSize 1
                result.options[0].name shouldBe "Option"
            }
        }

        `when`("존재하지 않는 상품 ID=999") {
            every { productPersistencePort.findById(999L) } returns null

            then("예외") {
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
        `when`("title=WEDDING, productType=WEDDING_SNAP, shootingPlace=JEJU, sortBy=created-desc") {
            val p1 = sampleProduct(productId = 10L, title = "P1")
            val p2 = sampleProduct(productId = 11L, title = "P2")

            // 검색 mock
            every {
                productPersistencePort.searchByCriteria("WEDDING", "WEDDING_SNAP", "JEJU", "created-desc")
            } returns listOf(p1, p2)

            then("결과 2개, 페이징 OK") {
                val result = productUseCase.searchProducts(
                    title = "WEDDING",
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

        `when`("검색 결과가 없는 경우") {
            every { productPersistencePort.searchByCriteria(any(), any(), any(), any()) } returns emptyList()

            then("0건") {
                val result = productUseCase.searchProducts(null, null, null, null, 0, 10)
                result.content shouldHaveSize 0
                result.totalElements shouldBe 0
                result.totalPages shouldBe 0
            }
        }
    }

    // ─────────────────────────────────────────
    // getMainPageProducts() 테스트

    given("getMainPageProducts") {
        val p1 = sampleProduct(productId = 1L, title = "Main1")
        val p2 = sampleProduct(productId = 2L, title = "Main2")

        `when`("전체 상품 2개") {
            every { productPersistencePort.findAll() } returns listOf(p1, p2)

            then("size=2, totalElements=2, totalPages=1") {
                val result = productUseCase.getMainPageProducts(0, 10)
                result.content shouldHaveSize 2
                result.content.map { it.title } shouldContainExactly listOf("Main1", "Main2")
                result.totalElements shouldBe 2
                result.totalPages shouldBe 1
            }
        }

        `when`("전체 상품이 없는 경우") {
            every { productPersistencePort.findAll() } returns emptyList()

            then("0건") {
                val result = productUseCase.getMainPageProducts(0, 10)
                result.content shouldHaveSize 0
                result.totalElements shouldBe 0
                result.totalPages shouldBe 0
            }
        }
    }

    given("deleteProduct") {
        // 기존 상품: 대표=2, 서브=3..6 (4장)
        val prodToDelete = sampleProduct(
            productId = 1L,
            mainImageId = 2L,
            subImageIds = listOf(3L, 4L, 5L, 6L)
        )

        `when`("존재하는 상품 삭제") {
            every { productPersistencePort.findById(1L) } returns prodToDelete
            every { productOptionPersistencePort.deleteAllByProductId(1L) } just Runs
            every { productPersistencePort.deleteById(1L) } just Runs

            // 이미지 삭제: 대표=2, 서브=3..6
            every { imageService.delete(2L) } just Runs
            every { imageService.delete(3L) } just Runs
            every { imageService.delete(4L) } just Runs
            every { imageService.delete(5L) } just Runs
            every { imageService.delete(6L) } just Runs

            then("상품, 옵션, 이미지 삭제") {
                productUseCase.deleteProduct(1L)

                verify(exactly = 1) { productPersistencePort.findById(1L) }
                verify(exactly = 1) { productOptionPersistencePort.deleteAllByProductId(1L) }
                verify(exactly = 1) { productPersistencePort.deleteById(1L) }

                // 대표+서브(4장)=총5장
                verify(exactly = 1) { imageService.delete(2L) }
                verify(exactly = 1) { imageService.delete(3L) }
                verify(exactly = 1) { imageService.delete(4L) }
                verify(exactly = 1) { imageService.delete(5L) }
                verify(exactly = 1) { imageService.delete(6L) }
            }
        }

        `when`("존재하지 않는 상품 삭제") {
            every { productPersistencePort.findById(999L) } returns null

            then("예외") {
                val ex = shouldThrow<IllegalArgumentException> {
                    productUseCase.deleteProduct(999L)
                }
                ex.message shouldBe "삭제할 상품이 존재하지 않습니다. ID: 999"
            }
        }
    }

    // ─────────────────────────────────────────
    // getProductById() 테스트

    given("getProductById") {
        `when`("존재하는 상품 ID=77L") {
            // mock된 상품, productId=77, subImages=111..114
            val p = sampleProduct(
                productId = 77L,
                mainImageId = 110L,
                subImageIds = listOf(111L, 112L, 113L, 114L),
                options = listOf(sampleOption(optionId = 10L, productId = 77L, name = "Option"))
            )
            every { productPersistencePort.findById(77L) } returns p
            every { productOptionPersistencePort.findByProductId(77L) } returns p.options

            then("Product + Option 반환") {
                val result = productUseCase.getProductById(77L)
                result.productId shouldBe 77L
                result.title shouldBe "Sample Product"
                result.options shouldHaveSize 1
                result.options[0].name shouldBe "Option"
            }
        }

        `when`("존재하지 않는 상품 ID=999") {
            every { productPersistencePort.findById(999L) } returns null

            then("예외") {
                val ex = shouldThrow<IllegalArgumentException> {
                    productUseCase.getProductById(999L)
                }
                ex.message shouldBe "상품을 찾을 수 없습니다. ID: 999"
            }
        }
    }
})
