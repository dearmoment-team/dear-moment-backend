package kr.kro.dearmoment.product.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.usecase.ProductUseCase
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class ProductUseCaseTest : StringSpec({

    lateinit var productOptionPersistencePort: ProductOptionPersistencePort
    lateinit var productEntityRetrievalPort: ProductEntityRetrievalPort
    lateinit var productPersistencePort: ProductPersistencePort
    lateinit var productUseCase: ProductUseCase

    beforeTest {
        productOptionPersistencePort = mockk(relaxed = true)
        productEntityRetrievalPort = mockk(relaxed = true)
        productPersistencePort = mockk(relaxed = true)
        productUseCase = ProductUseCase(productPersistencePort, productOptionPersistencePort, productEntityRetrievalPort)
    }

    "옵션 추가 시 기존 옵션이 없을 경우 새 옵션만 추가된다" {
        // given
        val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
        val productEntity = ProductEntity(
            productId = 1L,
            userId = 1L,
            title = "테스트 상품",
            description = "테스트 설명",
            price = 100000,
            typeCode = 1,
            createdAt = fixedNow,
            updatedAt = fixedNow,
            options = mutableListOf()
        )
        val newOptions = listOf(
            ProductOption(
                optionId = 0L,
                productId = 1L,
                name = "새 옵션",
                additionalPrice = 5000,
                description = "새 옵션 설명",
                createdAt = fixedNow,
                updatedAt = fixedNow,
            )
        )

        every { productEntityRetrievalPort.getProductEntityById(1L) } returns productEntity
        every { productOptionPersistencePort.findByProduct(any()) } returns emptyList()

        // when
        productUseCase.modifyProductOptions(1L, newOptions)

        // then
        verify(exactly = 0) { productOptionPersistencePort.deleteById(any()) }
        verify(exactly = 1) { productOptionPersistencePort.save(newOptions[0]) }
    }

    "옵션 삭제 시 모든 기존 옵션이 삭제된다" {
        // given
        val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
        val productEntity = ProductEntity(
            productId = 1L,
            userId = 1L,
            title = "테스트 상품",
            description = "테스트 설명",
            price = 100000,
            typeCode = 1,
            createdAt = fixedNow,
            updatedAt = fixedNow,
            options = mutableListOf()
        )
        val existingOptions = listOf(
            ProductOption(optionId = 1L, productId = 1L, name = "기존 옵션 1", additionalPrice = 1000, description = "기존 옵션 설명"),
            ProductOption(optionId = 2L, productId = 1L, name = "기존 옵션 2", additionalPrice = 2000, description = "기존 옵션 설명")
        )

        every { productEntityRetrievalPort.getProductEntityById(1L) } returns productEntity
        every { productOptionPersistencePort.findByProduct(any()) } returns existingOptions

        // when
        productUseCase.modifyProductOptions(1L, emptyList())

        // then
        verify(exactly = 2) { productOptionPersistencePort.deleteById(any()) }
        existingOptions.forEach { option ->
            verify { productOptionPersistencePort.deleteById(option.optionId) }
        }
    }

    "옵션 업데이트 시 중복된 옵션이 존재하면 업데이트를 적용하지 않는다" {
        // given
        val fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0)
        val productEntity = ProductEntity(
            productId = 1L,
            userId = 1L,
            title = "테스트 상품",
            description = "테스트 설명",
            price = 100000,
            typeCode = 1,
            createdAt = fixedNow,
            updatedAt = fixedNow,
            options = mutableListOf()
        )
        val existingOptions = listOf(
            ProductOption(optionId = 0L, productId = 1L, name = "새 옵션", additionalPrice = 5000, description = "새 옵션 설명", createdAt = fixedNow, updatedAt = fixedNow)
        )
        val newOptions = listOf(
            ProductOption(optionId = 0L, productId = 1L, name = "새 옵션", additionalPrice = 5000, description = "새 옵션 설명", createdAt = fixedNow, updatedAt = fixedNow)
        )

        every { productEntityRetrievalPort.getProductEntityById(1L) } returns productEntity
        every { productOptionPersistencePort.findByProduct(any()) } returns existingOptions

        // when
        productUseCase.modifyProductOptions(1L, newOptions)

        // then
        verify(exactly = 0) { productOptionPersistencePort.save(any()) } // 중복된 옵션이므로 저장되지 않아야 함
        verify(exactly = 0) { productOptionPersistencePort.deleteById(any()) } // 삭제되지 않아야 함
    }

    "존재하지 않는 제품 ID로 호출 시 예외를 던진다" {
        // given
        every { productEntityRetrievalPort.getProductEntityById(999L) } throws IllegalArgumentException("존재하지 않는 제품 ID입니다.")

        // when
        val exception = shouldThrow<IllegalArgumentException> {
            productUseCase.modifyProductOptions(999L, emptyList())
        }

        // then
        exception.message shouldBe "존재하지 않는 제품 ID입니다."
    }
})
