package kr.kro.dearmoment.product.application.usecase

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class ProductOptionUseCaseTest : BehaviorSpec({

    // 인터페이스 타입으로 선언하고 실제 구현체 주입
    val productPersistencePort = mockk<ProductPersistencePort>()
    val productOptionPersistencePort = mockk<ProductOptionPersistencePort>()
    val useCase: ProductOptionUseCase = ProductOptionUseCaseImpl(productOptionPersistencePort, productPersistencePort)

    val mockProduct =
        Product(
            productId = 1L,
            userId = 1L,
            title = "Test Product",
            description = "Test Description",
            price = 10000,
            typeCode = 1,
            shootingTime = LocalDateTime.now(),
            shootingLocation = "Test Location",
            numberOfCostumes = 1,
            partnerShops = listOf(PartnerShop(name = "Partner1", link = "http://partner1.com")),
            detailedInfo = "Test Info",
            warrantyInfo = "Test Warranty",
            contactInfo = "Test Contact",
            images = listOf("image1.jpg"),
            options = emptyList(),
        )

    val validRequest =
        CreateProductOptionRequest(
            name = "Option 1",
            additionalPrice = 5000,
            description = "Test option",
        )

    val savedDomainOption =
        ProductOption(
            optionId = 1L,
            productId = 1L,
            name = "Option 1",
            additionalPrice = 5000,
            description = "Test option",
        )

    Given("saveProductOption") {
        When("존재하지 않는 productId로 요청 시") {
            every { productPersistencePort.findById(999L) } returns null

            Then("IllegalArgumentException 발생") {
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        useCase.saveProductOption(999L, validRequest)
                    }
                exception shouldHaveMessage "Product not found: 999"
            }
        }

        When("중복된 옵션 이름이 존재할 경우") {
            every { productPersistencePort.findById(1L) } returns mockProduct
            every { productOptionPersistencePort.existsByProductIdAndName(1L, "Option 1") } returns true

            Then("IllegalArgumentException 발생") {
                val exception =
                    shouldThrow<IllegalArgumentException> {
                        useCase.saveProductOption(1L, validRequest)
                    }
                exception shouldHaveMessage "Duplicate option name: Option 1"
            }
        }

        When("유효한 옵션 정보일 경우") {
            every { productPersistencePort.findById(1L) } returns mockProduct
            every { productOptionPersistencePort.existsByProductIdAndName(1L, "Option 1") } returns false
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
            every { productOptionPersistencePort.findById(1L) } returns savedDomainOption

            Then("응답 DTO 반환") {
                val response = useCase.getProductOptionById(1L)
                response shouldBe ProductOptionResponse.fromDomain(savedDomainOption)
            }
        }

        When("존재하지 않는 옵션 ID로 요청 시") {
            every { productOptionPersistencePort.findById(999L) } throws IllegalArgumentException("Not found")

            Then("예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    useCase.getProductOptionById(999L)
                }
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
            every { productOptionPersistencePort.findByProductId(1L) } returns options

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
            every { productOptionPersistencePort.existsByProductId(1L) } returns true
            Then("true 반환") { useCase.existsProductOptions(1L) shouldBe true }
        }

        When("옵션이 존재하지 않는 경우") {
            every { productOptionPersistencePort.existsByProductId(1L) } returns false
            Then("false 반환") { useCase.existsProductOptions(1L) shouldBe false }
        }
    }
})
