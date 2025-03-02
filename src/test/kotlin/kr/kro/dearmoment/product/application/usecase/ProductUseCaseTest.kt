//package kr.kro.dearmoment.product.application.usecase
//
//import io.kotest.core.spec.style.BehaviorSpec
//import io.kotest.matchers.shouldBe
//import io.mockk.*
//import kr.kro.dearmoment.image.application.service.ImageService
//import kr.kro.dearmoment.image.domain.Image
//import kr.kro.dearmoment.product.application.dto.request.*
//import kr.kro.dearmoment.product.application.dto.response.ProductResponse
//import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
//import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
//import kr.kro.dearmoment.product.domain.model.*
//import org.springframework.web.multipart.MultipartFile
//import java.time.LocalDateTime
//
//class ProductUseCaseTest : BehaviorSpec({
//
//    // -------------------------------------------------------------------------
//    // 테스트 준비
//    // -------------------------------------------------------------------------
//    beforeEach {
//        clearAllMocks()
//    }
//
//    val productPersistencePort = mockk<ProductPersistencePort>()
//    val productOptionPersistencePort = mockk<ProductOptionPersistencePort>()
//    val imageService = mockk<ImageService>()
//    val imageHandler = ImageHandler(imageService)
//
//    val productUseCase = ProductUseCaseImpl(
//        productPersistencePort,
//        productOptionPersistencePort,
//        imageService,
//        imageHandler
//    )
//
//    // -------------------------------------------------------------------------
//    // 공통적으로 사용할 Mock multipartFile, Image 등 예시
//    // -------------------------------------------------------------------------
//    val mockFileA = mockk<MultipartFile>(relaxed = true) {
//        every { originalFilename } returns "fileA.jpg"
//    }
//    val mockFileB = mockk<MultipartFile>(relaxed = true) {
//        every { originalFilename } returns "fileB.jpg"
//    }
//    val mockFileC = mockk<MultipartFile>(relaxed = true) {
//        every { originalFilename } returns "fileC.jpg"
//    }
//
//    // 샘플 Image 도메인(기존 이미지 시나리오)
//    val existingImage1 = Image(
//        imageId = 101L,
//        userId = 999L,
//        fileName = "existing1.jpg",
//        url = "http://cdn.com/existing1.jpg",
//        urlExpireTime = LocalDateTime.now().plusDays(1)
//    )
//    val existingImage2 = Image(
//        imageId = 102L,
//        userId = 999L,
//        fileName = "existing2.jpg",
//        url = "http://cdn.com/existing2.jpg",
//        urlExpireTime = LocalDateTime.now().plusDays(1)
//    )
//    val existingImage3 = Image(
//        imageId = 103L,
//        userId = 999L,
//        fileName = "existing3.jpg",
//        url = "http://cdn.com/existing3.jpg",
//        urlExpireTime = LocalDateTime.now().plusDays(1)
//    )
//    val existingImage4 = Image(
//        imageId = 104L,
//        userId = 999L,
//        fileName = "existing4.jpg",
//        url = "http://cdn.com/existing4.jpg",
//        urlExpireTime = LocalDateTime.now().plusDays(1)
//    )
//    val existingImage5 = Image(
//        imageId = 105L,
//        userId = 999L,
//        fileName = "existing5.jpg",
//        url = "http://cdn.com/existing5.jpg",
//        urlExpireTime = LocalDateTime.now().plusDays(1)
//    )
//
//    // -------------------------------------------------------------------------
//    // "상품 업데이트" 상세 시나리오
//    // -------------------------------------------------------------------------
//    Given("이미 존재하는 상품이 있고, 여러가지 업데이트 상황이 발생한다") {
//
//        // 기존 상품 도메인 (서브이미지 4장, 추가이미지 0장, 옵션 2개)
//        val existingProduct = Product(
//            productId = 1L,
//            userId = 999L,
//            productType = ProductType.WEDDING_SNAP,
//            shootingPlace = ShootingPlace.JEJU,
//            title = "기존 상품명",
//            description = "기존 설명",
//            mainImage = existingImage1,
//            subImages = listOf(existingImage2, existingImage3, existingImage4, existingImage5),
//            additionalImages = emptyList(),
//            retouchStyles = setOf(RetouchStyle.MODERN),
//            options = listOf(
//                ProductOption(
//                    optionId = 10L,
//                    productId = 1L,
//                    name = "옵션A",
//                    optionType = OptionType.SINGLE,
//                    discountAvailable = false,
//                    originalPrice = 100000L,
//                    discountPrice = 0L,
//                    description = "",
//                    costumeCount = 1,
//                    shootingHours = 1,
//                    shootingLocationCount = 1,
//                    retouchedCount = 1
//                ),
//                ProductOption(
//                    optionId = 11L,
//                    productId = 1L,
//                    name = "옵션B",
//                    optionType = OptionType.SINGLE,
//                    discountAvailable = true,
//                    originalPrice = 200000L,
//                    discountPrice = 150000L,
//                    description = "",
//                    costumeCount = 2,
//                    shootingHours = 1,
//                    shootingLocationCount = 1,
//                    retouchedCount = 1
//                )
//            )
//        )
//
//        // Stubbing
//        every { productPersistencePort.findById(1L) } returns existingProduct
//        every { productPersistencePort.save(any()) } answers { firstArg<Product>() }
//        every { productOptionPersistencePort.findByProductId(1L) } returns existingProduct.options
//
//        // 옵션 삭제/저장
//        // (테스트는 옵션 부분 업데이트만 검증; 전체 삭제 로직은 없음)
//        justRun { productOptionPersistencePort.deleteById(any()) }
//        every { productOptionPersistencePort.save(any(), any()) } answers { firstArg<ProductOption>() }
//
//        // 이미지 업로드
//        coEvery { imageService.uploadSingleImage(any(), any()) } answers {
//            val file = arg<MultipartFile>(0)
//            val userId = arg<Long>(1)
//            val newId = (1000..2000).random().toLong()
//            Image(
//                imageId = newId,
//                userId = userId,
//                fileName = file.originalFilename ?: "unknown.jpg",
//                url = "http://cdn.com/${file.originalFilename}"
//            )
//        }
//        // 이미지 삭제
//        justRun { imageService.delete(any()) }
//
//        // --- 시나리오 1 ---
//        When("대표이미지 교체 + 서브이미지 전체교체(4장) + 추가이미지 없음 + 기존 옵션 일부 수정/삭제 + 새 옵션 추가") {
//            val subImagesFinalRequests = listOf(
//                SubImageFinalRequest(imageId = null, file = mockFileA),
//                SubImageFinalRequest(imageId = null, file = mockFileB),
//                SubImageFinalRequest(imageId = null, file = mockFileC),
//                SubImageFinalRequest(imageId = null, file = mockFileB), // 임의
//            )
//
//            val updateReq = UpdateProductRequest(
//                productId = 1L,
//                userId = 999L,
//                productType = "WEDDING_SNAP",
//                shootingPlace = "JEJU",
//                title = "바뀐 상품명",
//                description = "바뀐 설명",
//                mainImageFile = mockFileA,
//                subImagesFinal = subImagesFinalRequests,
//                additionalImagesFinal = emptyList(),
//                retouchStyles = listOf("CALM", "VINTAGE"),
//                options = listOf(
//                    UpdateProductOptionRequest(
//                        optionId = 10L, // 기존 옵션A
//                        name = "옵션A-수정",
//                        optionType = "SINGLE",
//                        originalPrice = 120000L,
//                        costumeCount = 2,
//                        shootingHours = 1,
//                        shootingLocationCount = 1,
//                        retouchedCount = 3
//                    ),
//                    UpdateProductOptionRequest(
//                        optionId = null, // 신규 옵션C
//                        name = "옵션C-추가",
//                        optionType = "SINGLE",
//                        originalPrice = 50000L,
//                        costumeCount = 2,
//                        shootingHours = 1,
//                        shootingLocationCount = 1,
//                        retouchedCount = 3
//                    )
//                    // 옵션B(11L)는 누락 => 삭제 대상
//                )
//            )
//
//            val updated: ProductResponse = productUseCase.updateProduct(updateReq)
//
//            Then("대표이미지는 새 파일로 교체, 기존(101L) 삭제") {
//                coVerify(exactly = 1) {
//                    imageService.uploadSingleImage(mockFileA, 999L)
//                    imageService.delete(101L)
//                }
//                updated.title shouldBe "바뀐 상품명"
//                updated.description shouldBe "바뀐 설명"
//            }
//
//            Then("서브이미지: 기존 102,103,104,105 전부 삭제 후 새 4장 업로드") {
//                // 업로드 4번 호출
//                coVerify(exactly = 4) { imageService.uploadSingleImage(ofType<MultipartFile>(), 999L) }
//                // 기존 4장 삭제
//                verify(exactly = 4) {
//                    imageService.delete(withArg { id -> id in listOf(102L, 103L, 104L, 105L) })
//                }
//                updated.subImages.size shouldBe 4
//            }
//
//            Then("추가이미지는 0장 유지") {
//                updated.additionalImages.size shouldBe 0
//            }
//
//            Then("보정스타일 = CALM, VINTAGE") {
//                updated.retouchStyles shouldBe setOf(RetouchStyle.CALM, RetouchStyle.VINTAGE)
//            }
//
//            Then("옵션A(10L)는 수정, 옵션B(11L)는 삭제, 옵션C(새로 추가)") {
//                verify { productOptionPersistencePort.deleteById(11L) }
//                // A 수정
//                verify {
//                    productOptionPersistencePort.save(
//                        match { it.optionId == 10L && it.originalPrice == 120000L },
//                        any()
//                    )
//                }
//                // C 추가
//                verify {
//                    productOptionPersistencePort.save(
//                        match { it.optionId == 0L && it.name == "옵션C-추가" },
//                        any()
//                    )
//                }
//                updated.options.size shouldBe 2
//            }
//        }
//
//        // --- 시나리오 2 ---
//        When("대표이미지는 그대로, 서브이미지 부분교체(2장 유지, 2장 교체), 추가이미지 2장 새로 추가, 옵션 일부 삭제") {
//            val subImagesPartial = listOf(
//                SubImageFinalRequest(102L, mockFileB), // 기존102->교체
//                SubImageFinalRequest(103L, mockFileC), // 기존103->교체
//                SubImageFinalRequest(104L, null),      // 유지
//                SubImageFinalRequest(105L, null)       // 유지
//            )
//            val additionalPartial = listOf(
//                AdditionalImageFinalRequest(null, mockFileA),
//                AdditionalImageFinalRequest(null, mockFileB),
//            )
//
//            val partialUpdateReq = UpdateProductRequest(
//                productId = 1L,
//                userId = 999L,
//                productType = "WEDDING_SNAP",
//                shootingPlace = "JEJU",
//                title = "상품명 부분변경",
//                description = "설명 부분변경",
//                mainImageFile = null,
//                subImagesFinal = subImagesPartial,
//                additionalImagesFinal = additionalPartial,
//                options = listOf(
//                    // 옵션A 유지(변경없음)
//                    UpdateProductOptionRequest(
//                        optionId = 10L,
//                        name = "옵션A(동일)",
//                        optionType = "SINGLE",
//                        originalPrice = 100000L,
//                        discountPrice = 0L,
//                        costumeCount = 2,
//                        shootingHours = 1,
//                        shootingLocationCount = 1,
//                        retouchedCount = 3
//                    )
//                    // 옵션B(11L)는 빠짐 -> 삭제
//                )
//            )
//
//            val updated2: ProductResponse = productUseCase.updateProduct(partialUpdateReq)
//
//            Then("대표이미지는 그대로(삭제/업로드 없음)") {
//                coVerify(exactly = 0) { imageService.delete(101L) }
//                coVerify(exactly = 0) { imageService.uploadSingleImage(mockFileA, 999L) } // 대표이미지 용 없음
//            }
//
//            Then("서브이미지 중 102,103만 교체 -> 기존삭제 + 새업로드, 104,105는 유지") {
//                coVerify(exactly = 1) { imageService.uploadSingleImage(mockFileB, 999L) } // 교체102
//                coVerify(exactly = 1) { imageService.uploadSingleImage(mockFileC, 999L) } // 교체103
//                verify(exactly = 1) { imageService.delete(102L) }
//                verify(exactly = 1) { imageService.delete(103L) }
//                verify(exactly = 0) { imageService.delete(104L) }
//                verify(exactly = 0) { imageService.delete(105L) }
//                updated2.subImages.size shouldBe 4
//            }
//
//            Then("추가이미지 2장 새로 추가되어 최종 2장") {
//                coVerify(exactly = 1) { imageService.uploadSingleImage(mockFileA, 999L) }
//                coVerify(exactly = 1) { imageService.uploadSingleImage(mockFileB, 999L) }
//                updated2.additionalImages.size shouldBe 2
//            }
//
//            Then("옵션B(11L)는 빠졌으므로 삭제, 옵션A(10L)는 그대로") {
//                verify { productOptionPersistencePort.deleteById(11L) }
//                verify(exactly = 1) {
//                    productOptionPersistencePort.save(
//                        withArg<ProductOption> {
//                            it.optionId shouldBe 10L
//                            it.name shouldBe "옵션A(동일)"
//                            it.originalPrice shouldBe 100000L
//                        },
//                        any()
//                    )
//                }
//                updated2.options.size shouldBe 1
//            }
//        }
//    }
//
//    // -------------------------------------------------------------------------
//    // 상품 삭제 테스트
//    // -------------------------------------------------------------------------
//
//    Given("상품 삭제가 요청되었을 때") {
//        val productIdToDelete = 1L
//
//        // 예시로, 기존 상품에는 대표+서브+추가 총 5장
//        val existingProduct = Product(
//            productId = productIdToDelete,
//            userId = 999L,
//            productType = ProductType.WEDDING_SNAP,
//            shootingPlace = ShootingPlace.JEJU,
//            title = "삭제될 상품",
//            mainImage = existingImage1,
//            subImages = listOf(existingImage2, existingImage3, existingImage4, existingImage5),
//            additionalImages = listOf(),
//            options = listOf(ProductOption(
//                optionId = 10L,
//                productId = 1L,
//                name = "옵션A",
//                optionType = OptionType.SINGLE,
//                discountAvailable = false,
//                originalPrice = 100000L,
//                discountPrice = 0L,
//                description = "",
//                costumeCount = 1,
//                shootingHours = 1,
//                shootingLocationCount = 1,
//                retouchedCount = 1,
//            ),
//                ProductOption(
//                    optionId = 11L,
//                    productId = 1L,
//                    name = "옵션B",
//                    optionType = OptionType.SINGLE,
//                    discountAvailable = true,
//                    originalPrice = 200000L,
//                    discountPrice = 150000L,
//                    description = "",
//                    costumeCount = 2,
//                    shootingHours = 1,
//                    shootingLocationCount = 1,
//                    retouchedCount = 1
//                ))
//        )
//
//        // Stubbing
//        every { productPersistencePort.findById(productIdToDelete) } returns existingProduct
//        justRun { productPersistencePort.deleteById(productIdToDelete) }
//        justRun { imageService.delete(any()) }
//
//        // 옵션 전체 삭제는 '자동' 또는 별도 로직이라고 가정
//        // => 테스트도 옵션 삭제 호출은 검증하지 않음
//
//        When("deleteProduct(productId)를 호출하면") {
//            productUseCase.deleteProduct(productIdToDelete)
//
//            Then("상품 PersistencePort.deleteById(1L) 호출 + 대표/서브/추가 이미지 삭제") {
//                verify(exactly = 1) { productPersistencePort.deleteById(1L) }
//
//                // 대표 + 서브이미지 총 5장 삭제
//                verify(exactly = 5) {
//                    imageService.delete(withArg { id ->
//                        id in listOf(101L, 102L, 103L, 104L, 105L)
//                    })
//                }
//            }
//        }
//    }
//})
