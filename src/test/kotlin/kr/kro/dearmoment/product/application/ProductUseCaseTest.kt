package kr.kro.dearmoment.product.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.verify
import io.mockk.every
import io.mockk.mockk
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.domain.model.ProductOption
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.usecase.ProductUseCase
import java.time.LocalDateTime

class ProductUseCaseTest : StringSpec({

    val productPersistencePort: ProductPersistencePort = mockk()
    val productOptionPersistencePort: ProductOptionPersistencePort = mockk(relaxed = true)
    val productEntityRetrievalPort: ProductEntityRetrievalPort = mockk()
    val productUseCase = ProductUseCase(productPersistencePort, productOptionPersistencePort, productEntityRetrievalPort)

    "옵션을 추가하거나 삭제할 수 있다" {
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
        val updatedOptions = listOf(
            ProductOption(optionId = 0L, productId = 1L, name = "옵션 C", additionalPrice = 15000, description = "옵션 C 설명", createdAt = fixedNow, updatedAt = fixedNow)
        )

        // Mock 설정
        every { productEntityRetrievalPort.getProductEntityById(1L) } returns productEntity
        every { productOptionPersistencePort.findByProduct(productEntity) } returns listOf(
            ProductOption(optionId = 1L, productId = 1L, name = "기존 옵션", additionalPrice = 1000, description = "기존 옵션 설명", createdAt = fixedNow, updatedAt = fixedNow)
        )
        every { productOptionPersistencePort.save(updatedOptions[0]) } returns updatedOptions[0]
        every { productOptionPersistencePort.deleteById(any()) } returns Unit

        // when
        productUseCase.modifyProductOptions(1L, updatedOptions)

        // then
        verify(exactly = 1) { productOptionPersistencePort.deleteById(1L) }
        verify(exactly = 1) { productOptionPersistencePort.save(updatedOptions[0]) }
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
            ProductOption(optionId = 0L, productId = 1L, name = "새 옵션", additionalPrice = 5000, description = "새 옵션 설명", createdAt = fixedNow, updatedAt = fixedNow)
        )

        every { productEntityRetrievalPort.getProductEntityById(1L) } returns productEntity
        every { productOptionPersistencePort.findByProduct(productEntity) } returns emptyList()
        every { productOptionPersistencePort.save(newOptions[0]) } returns newOptions[0]

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
        val updatedOptions = emptyList<ProductOption>()

        every { productEntityRetrievalPort.getProductEntityById(1L) } returns productEntity
        every { productOptionPersistencePort.findByProduct(productEntity) } returns listOf(
            ProductOption(optionId = 1L, productId = 1L, name = "기존 옵션 1", additionalPrice = 1000, description = "기존 옵션 설명", createdAt = fixedNow, updatedAt = fixedNow),
            ProductOption(optionId = 2L, productId = 1L, name = "기존 옵션 2", additionalPrice = 2000, description = "기존 옵션 설명", createdAt = fixedNow, updatedAt = fixedNow)
        )
        every { productOptionPersistencePort.deleteById(any()) } returns Unit

        // when
        productUseCase.modifyProductOptions(1L, updatedOptions)

        // then
        verify(exactly = 2) { productOptionPersistencePort.deleteById(any()) }
        verify(exactly = 0) { productOptionPersistencePort.save(any()) }
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
        val duplicatedOptions = listOf(
            ProductOption(optionId = 1L, productId = 1L, name = "중복 옵션", additionalPrice = 15000, description = "중복 옵션 설명", createdAt = fixedNow, updatedAt = fixedNow)
        )

        every { productEntityRetrievalPort.getProductEntityById(1L) } returns productEntity
        every { productOptionPersistencePort.findByProduct(productEntity) } returns duplicatedOptions

        // when
        productUseCase.modifyProductOptions(1L, duplicatedOptions)

        // then
        verify(exactly = 0) { productOptionPersistencePort.save(any()) }
        verify(exactly = 0) { productOptionPersistencePort.deleteById(any()) }
    }
})
