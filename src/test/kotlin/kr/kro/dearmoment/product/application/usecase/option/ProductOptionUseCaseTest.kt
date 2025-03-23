package kr.kro.dearmoment.product.application.usecase.option

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.port.out.GetProductOptionPort
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.OptionType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import java.time.LocalDateTime

class ProductOptionUseCaseTest : BehaviorSpec({

    // 인터페이스 타입으로 선언하고 실제 구현체 주입
    val productOptionPersistencePort = mockk<ProductOptionPersistencePort>()
    val getProductOptionPort = mockk<GetProductOptionPort>()
    val getProductPort = mockk<GetProductPort>()
    val useCase: ProductOptionUseCase =
        ProductOptionUseCaseImpl(
            productOptionPersistencePort,
            getProductOptionPort,
            getProductPort,
        )

    // 새로운 도메인 모델에 맞게 Product 객체 생성
    val mockProduct =
        Product(
            productId = 1L,
            userId = 1L,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = "Test Product",
            description = "Test Description",
            availableSeasons = emptySet(),
            cameraTypes = emptySet(),
            retouchStyles = emptySet(),
            mainImage = Image(userId = 1L, fileName = "main.jpg", url = "http://example.com/main.jpg"),
            subImages =
                listOf(
                    Image(userId = 1L, fileName = "sub1.jpg", url = "http://example.com/sub1.jpg"),
                    Image(userId = 1L, fileName = "sub2.jpg", url = "http://example.com/sub2.jpg"),
                    Image(userId = 1L, fileName = "sub3.jpg", url = "http://example.com/sub3.jpg"),
                    Image(userId = 1L, fileName = "sub4.jpg", url = "http://example.com/sub4.jpg"),
                ),
            additionalImages = emptyList(),
            detailedInfo = "Test Info",
            contactInfo = "Test Contact",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            options = emptyList(),
        )

    // 유효한 SINGLE 옵션 요청 생성 (단품 옵션의 경우 필수 값 설정)
    val validRequest =
        CreateProductOptionRequest(
            name = "Option 1",
            optionType = "SINGLE",
            discountAvailable = false,
            originalPrice = 5000,
            discountPrice = 0,
            description = "Test option",
            costumeCount = 1,
            shootingLocationCount = 1,
            shootingHours = 1,
            shootingMinutes = 0,
            retouchedCount = 1,
            partnerShops = emptyList(),
        )

    // 저장 후 반환되는 도메인 객체 생성
    val savedDomainOption =
        ProductOption(
            optionId = 1L,
            productId = 1L,
            name = "Option 1",
            optionType = OptionType.SINGLE,
            discountAvailable = false,
            originalPrice = 5000,
            discountPrice = 0,
            description = "Test option",
            costumeCount = 1,
            shootingLocationCount = 1,
            shootingHours = 1,
            shootingMinutes = 0,
            retouchedCount = 1,
            partnerShops = emptyList(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

    Given("saveProductOption") {
        When("존재하지 않는 productId로 요청 시") {
            every { getProductPort.findById(999L) } returns null

            Then("CustomException 발생") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.saveProductOption(999L, validRequest)
                    }
                exception shouldHaveMessage ErrorCode.PRODUCT_NOT_FOUND.message
            }
        }

        When("중복된 옵션 이름이 존재할 경우") {
            every { getProductPort.findById(1L) } returns mockProduct
            every { getProductOptionPort.existsByProductIdAndName(1L, "Option 1") } returns true

            Then("CustomException 발생") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.saveProductOption(1L, validRequest)
                    }
                exception shouldHaveMessage ErrorCode.DUPLICATE_OPTION_NAME.message
            }
        }

        When("유효한 옵션 정보일 경우") {
            every { getProductPort.findById(1L) } returns mockProduct
            every { getProductOptionPort.existsByProductIdAndName(1L, "Option 1") } returns false
            // 정적 팩토리 메서드를 사용하여 도메인 객체로 변환한 후 저장
            every { productOptionPersistencePort.save(any(), any()) } returns savedDomainOption

            Then("옵션이 정상 저장되고 응답 DTO 반환") {
                val response = useCase.saveProductOption(1L, validRequest)
                response shouldBe ProductOptionResponse.fromDomain(savedDomainOption)
                verify(exactly = 1) {
                    productOptionPersistencePort.save(
                        match { it.optionId == 0L },
                        mockProduct,
                    )
                }
            }
        }
    }

    Given("getProductOptionById") {
        When("존재하는 옵션 ID로 요청 시") {
            every { getProductOptionPort.findById(1L) } returns savedDomainOption

            Then("응답 DTO 반환") {
                val response = useCase.getProductOptionById(1L)
                response shouldBe ProductOptionResponse.fromDomain(savedDomainOption)
            }
        }

        When("존재하지 않는 옵션 ID로 요청 시") {
            every { getProductOptionPort.findById(999L) } throws CustomException(ErrorCode.OPTION_NOT_FOUND)

            Then("CustomException 발생") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.getProductOptionById(999L)
                    }
                exception shouldHaveMessage ErrorCode.OPTION_NOT_FOUND.message
            }
        }
    }

    Given("getProductOptionsByProductId") {
        val options =
            listOf(
                savedDomainOption,
                savedDomainOption.copy(optionId = 2L, name = "Option 2"),
            )

        When("특정 상품의 옵션 조회 시") {
            every { getProductOptionPort.findByProductId(1L) } returns options

            Then("DTO 리스트 반환") {
                val responses = useCase.getProductOptionsByProductId(1L)
                responses shouldHaveSize 2
                responses[0].name shouldBe "Option 1"
                responses[1].name shouldBe "Option 2"
            }
        }
    }

    Given("existsProductOptions") {
        When("옵션이 존재하는 경우") {
            every { getProductOptionPort.existsByProductId(1L) } returns true
            Then("true 반환") {
                useCase.existsProductOptions(1L) shouldBe true
            }
        }

        When("옵션이 존재하지 않는 경우") {
            every { getProductOptionPort.existsByProductId(1L) } returns false
            Then("false 반환") {
                useCase.existsProductOptions(1L) shouldBe false
            }
        }
    }
})
