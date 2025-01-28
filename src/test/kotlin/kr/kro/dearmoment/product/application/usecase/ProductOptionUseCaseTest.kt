package kr.kro.dearmoment.product.application.usecase
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class ProductOptionUseCaseTest : BehaviorSpec({

    val productPersistencePort = mockk<ProductPersistencePort>()
    val productOptionPersistencePort = mockk<ProductOptionPersistencePort>()
    val useCase = ProductOptionUseCase(productOptionPersistencePort, productPersistencePort)

    val mockProduct =  Product(
        productId = 1L,
        title = "Test Product",
        price = 10000,
        typeCode = 1,
        images = listOf("image1.jpg"),
        partnerShops = listOf(
            PartnerShop(
                name = "Test Partner",
                link = "https://partner.com"
            )
        )
    )

    val validOption = ProductOption(
        productId = 1L,
        name = "Option 1",
        additionalPrice = 5000
    )

    Given("saveProductOption") {
        When("productId가 없는 경우") {
            val invalidOption = validOption.copy(productId = null)

            Then("IllegalArgumentException 발생") {
                val exception = shouldThrow<IllegalArgumentException> {
                    useCase.saveProductOption(invalidOption)
                }
                exception shouldHaveMessage "Product ID must be provided"
            }
        }

        When("존재하지 않는 productId로 요청 시") {
            every { productPersistencePort.findById(any()) } returns null

            Then("IllegalArgumentException 발생") {
                val exception = shouldThrow<IllegalArgumentException> {
                    useCase.saveProductOption(validOption)
                }
                exception shouldHaveMessage "Product with ID 1 not found"
            }
        }

        When("중복된 옵션 이름이 존재할 경우") {
            every { productPersistencePort.findById(any()) } returns mockProduct
            every { productOptionPersistencePort.findByProductId(any()) } returns listOf(
                validOption.copy(optionId = 1L)
            )

            Then("IllegalArgumentException 발생") {
                val exception = shouldThrow<IllegalArgumentException> {
                    useCase.saveProductOption(validOption)
                }
                exception shouldHaveMessage "Duplicate option name: Option 1"
            }
        }

        When("유효한 옵션 정보일 경우") {
            val savedOption = validOption.copy(optionId = 1L, createdAt = LocalDateTime.now())
            every { productPersistencePort.findById(any()) } returns mockProduct
            every { productOptionPersistencePort.findByProductId(any()) } returns emptyList()
            every { productOptionPersistencePort.save(any(), any()) } returns savedOption

            Then("옵션이 정상 저장되고 결과 반환") {
                val result = useCase.saveProductOption(validOption)

                result shouldBe savedOption
                verify(exactly = 1) { productOptionPersistencePort.save(validOption, mockProduct) }
            }
        }
    }

    Given("getProductOptionById") {
        val optionId = 1L
        val mockOption = validOption.copy(optionId = optionId)

        When("존재하는 옵션 ID로 요청 시") {
            every { productOptionPersistencePort.findById(optionId) } returns mockOption

            Then("옵션 정보 반환") {
                val result = useCase.getProductOptionById(optionId)
                result shouldBe mockOption
            }
        }

        When("존재하지 않는 옵션 ID로 요청 시") {
            every { productOptionPersistencePort.findById(any()) } throws IllegalArgumentException("Not found")

            Then("예외 발생") {
                shouldThrow<IllegalArgumentException> {
                    useCase.getProductOptionById(999L)
                }
            }
        }
    }

    Given("deleteProductOptionById") {
        val optionId = 1L

        When("정상 삭제 요청 시") {
            every { productOptionPersistencePort.deleteById(optionId) } returns Unit

            Then("삭제 메서드 호출 확인") {
                useCase.deleteProductOptionById(optionId)
                verify(exactly = 1) { productOptionPersistencePort.deleteById(optionId) }
            }
        }
    }

    Given("getProductOptionsByProductId") {
        val productId = 1L
        val mockOptions = listOf(
            validOption.copy(optionId = 1L),
            validOption.copy(optionId = 2L, name = "Option 2")
        )

        When("특정 상품의 옵션 조회 시") {
            every { productOptionPersistencePort.findByProductId(productId) } returns mockOptions

            Then("해당 상품의 옵션 목록 반환") {
                val result = useCase.getProductOptionsByProductId(productId)
                result shouldBe mockOptions
                result.size shouldBe 2
            }
        }
    }

    Given("existsProductOptions") {
        val productId = 1L

        When("옵션이 존재하는 경우") {
            every { productOptionPersistencePort.existsByProductId(productId) } returns true

            Then("true 반환") {
                useCase.existsProductOptions(productId) shouldBe true
            }
        }

        When("옵션이 존재하지 않는 경우") {
            every { productOptionPersistencePort.existsByProductId(productId) } returns false

            Then("false 반환") {
                useCase.existsProductOptions(productId) shouldBe false
            }
        }
    }

    Given("deleteAllProductOptionsByProductId") {
        val productId = 1L

        When("정상 삭제 요청 시") {
            every { productOptionPersistencePort.deleteAllByProductId(productId) } returns Unit

            Then("삭제 메서드 호출 확인") {
                useCase.deleteAllProductOptionsByProductId(productId)
                verify(exactly = 1) { productOptionPersistencePort.deleteAllByProductId(productId) }
            }
        }
    }
})