package kr.kro.dearmoment.product.application.usecase.create

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.user.application.port.output.GetStudioUserPort
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDateTime
import java.util.UUID

class CreateProductUseCaseTest : BehaviorSpec({

    // 목 객체 생성
    val productPersistencePort = mockk<ProductPersistencePort>()
    val getProductPort = mockk<GetProductPort>()
    val imageService = mockk<ImageService>()
    val getStudioUserPort = mockk<GetStudioUserPort>()

    val useCase = CreateProductUseCaseImpl(
        productPersistencePort = productPersistencePort,
        imageService = imageService,
        getProductPort = getProductPort,
        getStudioUserPort = getStudioUserPort
    )

    // 더미 파일 생성
    val dummyMainFile = MockMultipartFile("mainImageFile", "main.jpg", "image/jpeg", "dummy".toByteArray())
    val dummySubFiles = List(4) { index ->
        MockMultipartFile("subImageFiles", "sub$index.jpg", "image/jpeg", "dummy".toByteArray())
    }
    val dummyAdditionalFiles = listOf(
        MockMultipartFile("additionalImageFiles", "add1.jpg", "image/jpeg", "dummy".toByteArray())
    )

    // dummy user id (UUID)
    val dummyUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

    // 유효한 상품 생성 요청 객체 (userId 필드는 이제 UUID 타입)
    val validRequest = CreateProductRequest(
        studioId = 1L,
        productType = "WEDDING_SNAP",
        shootingPlace = "JEJU",
        title = "Unique Product",
        description = "Test Description",
        availableSeasons = listOf("YEAR_2025_FIRST_HALF"),
        cameraTypes = listOf("FILM"),
        retouchStyles = listOf("MODERN"),
        detailedInfo = "Test Info",
        contactInfo = "Test Contact",
        options = listOf(
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
                partnerShops = emptyList()
            )
        )
    )

    // 더미 이미지 객체
    val dummyImage = Image(
        userId = dummyUserId,
        fileName = "dummy.jpg",
        url = "http://example.com/dummy.jpg",
        parId = ""
    )

    // 중복된 제목 검증 시나리오
    Given("중복된 제목 검증 시나리오") {
        When("중복된 제목이 존재할 경우") {
            every {
                getProductPort.existsByUserIdAndTitle(userId = dummyUserId, title = "Unique Product")
            } returns true

            // 이미지 업로드 성공 모킹
            every { imageService.save(any<SaveImageCommand>()) } returns dummyImage

            // 스튜디오 사용자 검증: 스튜디오 권한이 있는 사용자를 반환
            every { getStudioUserPort.findStudioUserById(dummyUserId) } returns
                    kr.kro.dearmoment.user.domain.User(
                        id = dummyUserId,
                        loginId = "dummy",
                        password = "dummy",
                        name = "Studio User",
                        isStudio = true,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now(),
                        kakaoId = null
                    )

            Then("CustomException이 발생해야 한다") {
                val exception = shouldThrow<CustomException> {
                    useCase.saveProduct(validRequest, dummyUserId, dummyMainFile, dummySubFiles, dummyAdditionalFiles)
                }
                exception shouldHaveMessage ErrorCode.PRODUCT_ALREADY_EXISTS.message
                verify(exactly = 1) {
                    getProductPort.existsByUserIdAndTitle(dummyUserId, "Unique Product")
                }
            }
        }
    }

    // 이미지 개수 검증 시나리오
    Given("이미지 개수 검증 시나리오") {
        When("서브 이미지가 4장 미만일 경우") {
            val invalidSubFiles = dummySubFiles.take(3)
            every { getStudioUserPort.findStudioUserById(dummyUserId) } returns
                    kr.kro.dearmoment.user.domain.User(
                        id = dummyUserId,
                        loginId = "dummy",
                        password = "dummy",
                        name = "Studio User",
                        isStudio = true,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now(),
                        kakaoId = null
                    )
            Then("CustomException이 발생해야 한다") {
                val exception = shouldThrow<CustomException> {
                    useCase.saveProduct(validRequest, dummyUserId, dummyMainFile, invalidSubFiles, dummyAdditionalFiles)
                }
                exception shouldHaveMessage ErrorCode.INVALID_SUB_IMAGE_COUNT.message
            }
        }

        When("추가 이미지가 5장 초과일 경우") {
            val invalidAdditionalFiles = List(6) { dummyAdditionalFiles[0] }
            every { getStudioUserPort.findStudioUserById(dummyUserId) } returns
                    kr.kro.dearmoment.user.domain.User(
                        id = dummyUserId,
                        loginId = "dummy",
                        password = "dummy",
                        name = "Studio User",
                        isStudio = true,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now(),
                        kakaoId = null
                    )
            Then("CustomException이 발생해야 한다") {
                val exception = shouldThrow<CustomException> {
                    useCase.saveProduct(validRequest, dummyUserId, dummyMainFile, dummySubFiles, invalidAdditionalFiles)
                }
                exception shouldHaveMessage ErrorCode.INVALID_ADDITIONAL_IMAGE_COUNT.message
            }
        }
    }

    // 스튜디오 권한 검증 시나리오
    Given("스튜디오 권한이 없는 사용자가 상품 생성 요청 시") {
        // 모킹: 스튜디오 권한이 없음을 나타내도록 예외 발생
        every { getStudioUserPort.findStudioUserById(dummyUserId) } throws CustomException(ErrorCode.UNAUTHORIZED_ACCESS)
        When("상품 생성 요청 시") {
            Then("CustomException이 발생해야 한다") {
                val exception = shouldThrow<CustomException> {
                    useCase.saveProduct(validRequest, dummyUserId, dummyMainFile, dummySubFiles, dummyAdditionalFiles)
                }
                exception shouldHaveMessage ErrorCode.UNAUTHORIZED_ACCESS.message
            }
        }
    }

    // 정상 생성 시나리오 (옵션 리스트 포함)
    Given("정상 생성 시나리오 (옵션 리스트 포함)") {
        // 스튜디오 권한 검증: 스튜디오 사용자를 반환
        every { getStudioUserPort.findStudioUserById(dummyUserId) } returns
                kr.kro.dearmoment.user.domain.User(
                    id = dummyUserId,
                    loginId = "dummy",
                    password = "dummy",
                    name = "Studio User",
                    isStudio = true,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                    kakaoId = null
                )
        val dummyProduct = Product(
            productId = 1L,
            userId = dummyUserId,
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

        every { getProductPort.existsByUserIdAndTitle(any(), any()) } returns false
        every { imageService.save(any<SaveImageCommand>()) } returns dummyImage
        every { productPersistencePort.save(any(), 1L) } returns dummyProduct
        every { getProductPort.findById(1L) } returns dummyProduct

        When("모든 조건을 만족하여 상품 생성 시") {
            val result = useCase.saveProduct(validRequest, dummyUserId, dummyMainFile, dummySubFiles, dummyAdditionalFiles)

            Then("상품 저장 로직이 수행되고, 옵션은 별도 저장되어 결과 옵션 리스트는 빈 리스트여야 한다") {
                verify(exactly = 1) {
                    productPersistencePort.save(
                        match { product ->
                            product.title == "Unique Product" &&
                                    product.userId == dummyUserId
                        },
                        1L,
                    )
                }
                result.options.size shouldBe 0
            }
        }
    }
})
