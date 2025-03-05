package kr.kro.dearmoment.product.application.usecase.create

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.usecase.option.ProductOptionUseCase
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDateTime

class CreateProductUseCaseTest : BehaviorSpec({

    val productPersistencePort = mockk<ProductPersistencePort>()
    val imageService = mockk<ImageService>()
    val productOptionUseCase = mockk<ProductOptionUseCase>(relaxed = true)

    val useCase =
        CreateProductUseCaseImpl(
            productPersistencePort = productPersistencePort,
            imageService = imageService,
            productOptionUseCase = productOptionUseCase,
        )

    val dummyMainFile = MockMultipartFile("mainImageFile", "main.jpg", "image/jpeg", "dummy".toByteArray())
    val dummySubFiles =
        List(4) {
            MockMultipartFile("subImageFiles", "sub$it.jpg", "image/jpeg", "dummy".toByteArray())
        }
    val dummyAdditionalFiles =
        listOf(
            MockMultipartFile("additionalImageFiles", "add1.jpg", "image/jpeg", "dummy".toByteArray()),
        )

    val validRequest =
        CreateProductRequest(
            userId = 1L,
            productType = "WEDDING_SNAP",
            shootingPlace = "JEJU",
            title = "Unique Product",
            description = "Test Description",
            availableSeasons = listOf("YEAR_2025_FIRST_HALF"),
            cameraTypes = listOf("FILM"),
            retouchStyles = listOf("NATURAL"),
            mainImageFile = dummyMainFile,
            subImageFiles = dummySubFiles,
            additionalImageFiles = dummyAdditionalFiles,
            detailedInfo = "Test Info",
            contactInfo = "Test Contact",
            options =
                listOf(
                    CreateProductOptionRequest(
                        name = "Option A",
                        optionType = "SINGLE",
                        discountAvailable = false,
                        originalPrice = 10000,
                        discountPrice = 8000,
                        description = "Option description",
                        costumeCount = 1,
                        shootingLocationCount = 1,
                        shootingHours = 1,
                        shootingMinutes = 0,
                        retouchedCount = 1,
                        partnerShops = emptyList(),
                    ),
                ),
        )

    val dummyImage =
        Image(
            userId = 1L,
            fileName = "dummy.jpg",
            url = "http://example.com/dummy.jpg",
        )

    Given("중복된 제목 검증 시나리오") {
        When("중복된 제목이 존재할 경우") {
            every {
                productPersistencePort.existsByUserIdAndTitle(
                    userId = 1L,
                    title = "Unique Product",
                )
            } returns true

            // 이미지 업로드 성공 모킹 (save 메서드 사용)
            every { imageService.save(any<SaveImageCommand>()) } returns dummyImage

            Then("IllegalArgumentException 발생") {
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        useCase.saveProduct(validRequest)
                    }

                exception shouldHaveMessage "동일 제목의 상품이 이미 존재합니다: Unique Product"

                verify(exactly = 1) {
                    productPersistencePort.existsByUserIdAndTitle(1L, "Unique Product")
                }
            }
        }
    }

    Given("이미지 개수 검증 시나리오") {
        When("서브 이미지가 4장 미만일 경우") {
            val invalidRequest =
                validRequest.copy(
                    subImageFiles = dummySubFiles.take(3),
                )

            Then("IllegalArgumentException 발생") {
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        useCase.saveProduct(invalidRequest)
                    }

                exception.message shouldBe "서브 이미지는 정확히 4장이어야 합니다. 현재 3장입니다."
            }
        }

        When("추가 이미지가 5장 초과일 경우") {
            val invalidRequest =
                validRequest.copy(
                    additionalImageFiles = List(6) { dummyAdditionalFiles[0] },
                )

            Then("IllegalArgumentException 발생") {
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        useCase.saveProduct(invalidRequest)
                    }

                exception.message shouldBe "추가 이미지는 최대 5장까지만 가능합니다. 현재 6장입니다."
            }
        }
    }

    Given("정상 생성 시나리오") {
        val dummyProduct =
            Product(
                productId = 1L,
                userId = 1L,
                productType = ProductType.WEDDING_SNAP,
                shootingPlace = ShootingPlace.JEJU,
                title = "Unique Product",
                description = "Test Description",
                availableSeasons = setOf(),
                cameraTypes = setOf(),
                retouchStyles = setOf(),
                mainImage = dummyImage,
                subImages = List(4) { dummyImage },
                additionalImages = listOf(dummyImage),
                detailedInfo = "Test Info",
                contactInfo = "Test Contact",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                options = emptyList(),
            )

        every { productPersistencePort.existsByUserIdAndTitle(any(), any()) } returns false
        every { imageService.save(any<SaveImageCommand>()) } returns dummyImage
        every { productPersistencePort.save(any()) } returns dummyProduct

        When("모든 조건을 만족할 경우") {
            val result = useCase.saveProduct(validRequest)

            Then("상품 저장 및 옵션 생성 로직 수행") {
                verify(exactly = 1) {
                    productPersistencePort.save(
                        match { product ->
                            product.title == "Unique Product" &&
                                product.userId == 1L
                        },
                    )
                }
                verify(exactly = validRequest.options.size) {
                    productOptionUseCase.saveProductOption(
                        eq(1L),
                        match { optionReq ->
                            optionReq.name == "Option A"
                        },
                    )
                }
                result shouldBe ProductResponse.fromDomain(dummyProduct)
            }
        }
    }
})
