package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class ProductOptionEntityTest : StringSpec({

    "ProductOptionEntity should convert from domain model correctly" {
        val productEntity = ProductEntity(
            productId = 1L,
            title = "Test Product",
            price = 1000L,
            typeCode = 1
        )

        val productOption = ProductOption(
            optionId = 1L,
            productId = 1L,
            name = "Test Option",
            additionalPrice = 500L,
            description = "Option description",
            createdAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0),
            updatedAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        )

        val productOptionEntity = ProductOptionEntity.fromDomain(productOption, productEntity)


        productOptionEntity.optionId shouldBe productOption.optionId
        productOptionEntity.name shouldBe productOption.name
        productOptionEntity.additionalPrice shouldBe productOption.additionalPrice
        productOptionEntity.description shouldBe productOption.description
        productOptionEntity.product?.productId shouldBe productOption.productId
        productOptionEntity.createdAt shouldBe productOption.createdAt
        productOptionEntity.updatedAt shouldBe productOption.updatedAt
    }

    "ProductOptionEntity should convert to domain model correctly" {
        // given: ProductOptionEntity 객체 준비
        val productEntity = ProductEntity(
            productId = 1L,
            title = "Test Product",
            price = 1000L,
            typeCode = 1
        )

        val productOptionEntity = ProductOptionEntity(
            optionId = 1L,
            name = "Test Option",
            additionalPrice = 500L,
            description = "Option description",
            product = productEntity,
            createdAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0),
            updatedAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)
        )

        // when: ProductOptionEntity에서 도메인 모델로 변환
        val productOption = productOptionEntity.toDomain()

        // then: 변환된 도메인 모델의 필드들이 올바르게 설정되었는지 확인
        productOption.optionId shouldBe productOptionEntity.optionId
        productOption.name shouldBe productOptionEntity.name
        productOption.additionalPrice shouldBe productOptionEntity.additionalPrice
        productOption.description shouldBe productOptionEntity.description
        productOption.productId shouldBe productOptionEntity.product?.productId
        productOption.createdAt shouldBe productOptionEntity.createdAt
        productOption.updatedAt shouldBe productOptionEntity.updatedAt
    }
})
