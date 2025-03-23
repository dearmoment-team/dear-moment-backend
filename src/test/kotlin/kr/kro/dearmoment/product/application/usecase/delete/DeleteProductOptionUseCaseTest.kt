package kr.kro.dearmoment.product.application.usecase.delete

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.OptionType
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class DeleteProductOptionUseCaseTest : BehaviorSpec({
    val productOptionPersistencePort = mockk<ProductOptionPersistencePort>(relaxed = true)
    val useCase = DeleteProductOptionUseCaseImpl(productOptionPersistencePort)
    val validOption =
        ProductOption(
            optionId = 100L,
            productId = 1L,
            name = "Dummy Option",
            optionType = OptionType.SINGLE,
            discountAvailable = false,
            originalPrice = 1000,
            discountPrice = 800,
            description = "Dummy",
            costumeCount = 1,
            shootingLocationCount = 1,
            shootingHours = 1,
            shootingMinutes = 0,
            retouchedCount = 1,
            partnerShops = emptyList(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

    Given("존재하는 옵션이 있을 때") {
        When("deleteOption이 productId가 일치하는 경우 호출되면") {
            every { productOptionPersistencePort.findById(100L) } returns validOption

            Then("옵션이 삭제되어야 한다") {
                useCase.deleteOption(1L, 100L)
                verify(exactly = 1) { productOptionPersistencePort.deleteById(100L) }
            }
        }

        When("deleteOption이 productId가 일치하지 않는 경우 호출되면") {
            every { productOptionPersistencePort.findById(100L) } returns validOption

            Then("CustomException이 발생해야 한다") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.deleteOption(2L, 100L)
                    }
                exception.errorCode shouldBe ErrorCode.INVALID_REQUEST
            }
        }
    }
})
