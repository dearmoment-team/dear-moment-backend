package kr.kro.dearmoment.product.application.usecase

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class ProductOptionUseCaseTest : StringSpec({

    // Mock 초기화
    val productOptionPersistencePort: ProductOptionPersistencePort = mockk()
    val productEntityRetrievalPort: ProductEntityRetrievalPort = mockk()
    val productOptionUseCase = ProductOptionUseCase(productOptionPersistencePort, productEntityRetrievalPort)

    "중복된 옵션 이름 저장 시 예외 발생" {
        // given
        val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
        val product = mockk<Product>()
        val existingOptions =
            listOf(
                ProductOption(1L, 1L, "중복 옵션", 5000, "중복 옵션 설명", fixedNow, fixedNow),
            )
        val newOption = ProductOption(2L, 1L, "중복 옵션", 10000, "또 다른 중복 옵션 설명", fixedNow, fixedNow)

        // Mock 설정: getProductEntityById 호출 및 findByProduct 동작 설정
        every { productEntityRetrievalPort.getProductById(1L) } returns product
        every { productOptionPersistencePort.findByProduct(product) } returns existingOptions

        // when & then
        val exception =
            shouldThrow<IllegalArgumentException> {
                productOptionUseCase.saveProductOption(newOption)
            }

        // 예외 메시지 검증
        exception.message shouldBe "Duplicate option name: 중복 옵션"

        // 호출 검증
        verify(exactly = 1) { productEntityRetrievalPort.getProductById(1L) }
        verify(exactly = 1) { productOptionPersistencePort.findByProduct(product) }
    }
})
