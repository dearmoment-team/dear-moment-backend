package kr.kro.dearmoment.product.application

import io.kotest.core.spec.style.StringSpec
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
})
