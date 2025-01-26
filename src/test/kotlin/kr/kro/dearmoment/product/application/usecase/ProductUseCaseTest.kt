package kr.kro.dearmoment.product.application.usecase

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.junit5.MockKExtension
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class ProductUseCaseTest : BehaviorSpec({

    val productPersistencePort: ProductPersistencePort = mockk(relaxed = true)
    val productOptionPersistencePort: ProductOptionPersistencePort = mockk(relaxed = true)
    val productEntityRetrievalPort: ProductEntityRetrievalPort = mockk(relaxed = true)
    val productUseCase = ProductUseCase(
        productPersistencePort,
        productOptionPersistencePort,
        productEntityRetrievalPort
    )

    Given("ProductUseCase의 modifyProductOptions 메서드") {

        When("옵션 추가 시 기존 옵션이 없을 경우 새 옵션만 추가된다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val product = Product(
                productId = 1L,
                userId = 1L,
                title = "Test Product",
                description = "Test Description",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = emptyList()
            )
            val newOption = ProductOption(
                optionId = 0L,
                productId = 1L,
                name = "New Option",
                additionalPrice = 5000,
                description = "New Option Description",
                createdAt = fixedNow,
                updatedAt = fixedNow
            )

            every { productEntityRetrievalPort.getProductById(1L) } returns product
            every { productOptionPersistencePort.findByProduct(any()) } returns emptyList()

            // when
            productUseCase.modifyProductOptions(1L, listOf(newOption))

            // then
            verify(exactly = 1) { productOptionPersistencePort.save(newOption) }
            verify(exactly = 0) { productOptionPersistencePort.deleteById(any()) }
        }

        When("옵션이 중복된 경우 저장하지 않는다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val product = Product(
                productId = 1L,
                userId = 1L,
                title = "Test Product",
                description = "Test Description",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = emptyList()
            )
            val existingOption = ProductOption(
                optionId = 1L,
                productId = 1L,
                name = "Existing Option",
                additionalPrice = 5000,
                description = "Existing Option Description",
                createdAt = fixedNow,
                updatedAt = fixedNow
            )

            every { productEntityRetrievalPort.getProductById(1L) } returns product
            every { productOptionPersistencePort.findByProduct(any()) } returns listOf(existingOption)

            // when
            productUseCase.modifyProductOptions(1L, listOf(existingOption))

            // then
            verify(exactly = 0) { productOptionPersistencePort.save(any()) }
        }

        When("존재하지 않는 Product ID로 호출 시 예외를 던진다") {
            // given
            every { productEntityRetrievalPort.getProductById(999L) } throws IllegalArgumentException("존재하지 않는 제품 ID입니다.")

            // when
            val exception = shouldThrow<IllegalArgumentException> {
                productUseCase.modifyProductOptions(999L, emptyList())
            }

            // then
            exception.message shouldBe "존재하지 않는 제품 ID입니다."
        }

        When("옵션 리스트가 비어 있는 경우 저장 또는 삭제가 일어나지 않는다") {
            // given
            val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
            val product = Product(
                productId = 1L,
                userId = 1L,
                title = "Test Product",
                description = "Test Description",
                price = 100000,
                typeCode = 1,
                createdAt = fixedNow,
                updatedAt = fixedNow,
                options = emptyList()
            )

            every { productEntityRetrievalPort.getProductById(1L) } returns product
            every { productOptionPersistencePort.findByProduct(any()) } returns emptyList()

            // when
            productUseCase.modifyProductOptions(1L, emptyList())

            // then
            verify(exactly = 0) { productOptionPersistencePort.save(any()) }
            verify(exactly = 0) { productOptionPersistencePort.deleteById(any()) }
        }
    }
})



