package kr.kro.dearmoment.product.application.usecase.delete

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.product.application.port.out.GetProductOptionPort
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.OptionType
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime
import java.util.UUID

class DeleteProductOptionUseCaseTest : BehaviorSpec({
    // 목 객체 생성 (GetProductPort도 추가)
    val productOptionPersistencePort = mockk<ProductOptionPersistencePort>(relaxed = true)
    val getProductOptionPort = mockk<GetProductOptionPort>(relaxed = true)
    val getProductPort = mockk<GetProductPort>(relaxed = true)
    val useCase = DeleteProductOptionUseCaseImpl(productOptionPersistencePort, getProductOptionPort, getProductPort)

    // dummy userId (인증된 사용자)
    val dummyUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

    // 유효한 옵션 객체 (해당 옵션이 상품 1L에 속함)
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

    // 상품 조회를 위한 더미 상품 (상품의 소유자가 dummyUserId인 경우)
    val validProduct = mockk<ProductOption>() // 실제 사용은 옵션 객체 대신, 상품 객체를 사용해야 하지만 여기서는 소유권 검증을 위해 getProductPort 모킹이 필요합니다.
    // 단순하게 상품의 userId를 반환하는 목업용 객체를 정의합니다.
    // 여기서는 getProductPort.findById를 모킹하여, 상품의 소유자가 dummyUserId인 경우와 아닌 경우를 테스트합니다.

    Given("존재하는 옵션이 있을 때") {
        When("deleteOption이 상품 ID가 일치하는 경우 호출되면") {
            every { getProductOptionPort.findById(100L) } returns validOption
            // getProductPort에서 상품을 조회 시, 상품의 userId가 dummyUserId로 설정된 상품 객체를 반환
            every { getProductPort.findById(1L) } returns
                mockk(relaxed = true) {
                    every { userId } returns dummyUserId
                }

            Then("옵션이 삭제되어야 한다") {
                useCase.deleteOption(dummyUserId, 1L, 100L)
                verify(exactly = 1) { productOptionPersistencePort.deleteById(100L) }
            }
        }

        When("deleteOption이 상품 ID가 일치하지 않는 경우 호출되면") {
            every { getProductOptionPort.findById(100L) } returns validOption
            // 상품의 소유자가 다른 경우: 예를 들어, 상품의 userId가 다른 UUID
            val otherUserId = UUID.fromString("11111111-2222-3333-4444-555555555555")
            every { getProductPort.findById(1L) } returns
                mockk(relaxed = true) {
                    every { userId } returns otherUserId
                }

            Then("CustomException이 발생해야 한다") {
                val exception =
                    shouldThrow<CustomException> {
                        useCase.deleteOption(dummyUserId, 1L, 100L)
                    }
                exception.errorCode shouldBe ErrorCode.INVALID_REQUEST
            }
        }
    }
})
