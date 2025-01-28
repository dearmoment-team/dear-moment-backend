package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class ProductOptionEntityTest : StringSpec({
    "ProductOptionEntity는 도메인 모델에서 올바르게 변환되어야 한다" {
        val fixedCreatedAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val fixedUpdatedAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)

        val partnerShops =
            listOf(
                PartnerShopEmbeddable(name = "상점1", link = "http://shop1.com"),
                PartnerShopEmbeddable(name = "상점2", link = "http://shop2.com"),
            )
        val productEntity =
            ProductEntity(
                productId = 1L,
                userId = 123L,
                title = "테스트 제품",
                description = "이것은 테스트 제품입니다",
                price = 1000L,
                typeCode = 1,
                shootingTime = fixedCreatedAt,
                shootingLocation = "테스트 장소",
                numberOfCostumes = 5,
                partnerShops = partnerShops,
                detailedInfo = "상세 정보",
                warrantyInfo = "1년 보증",
                contactInfo = "test@example.com",
                createdAt = fixedCreatedAt,
                updatedAt = fixedUpdatedAt,
                images = listOf("image1.jpg", "image2.jpg"),
            )

        val productOption =
            ProductOption(
                optionId = 1L,
                productId = 1L,
                name = "테스트 옵션",
                additionalPrice = 500L,
                description = "옵션 설명",
                createdAt = fixedCreatedAt,
                updatedAt = fixedUpdatedAt,
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

    "ProductOptionEntity는 도메인 모델로 올바르게 변환되어야 한다" {
        val fixedCreatedAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val fixedUpdatedAt = LocalDateTime.of(2023, 1, 1, 12, 0, 0)

        val partnerShops =
            listOf(
                PartnerShopEmbeddable(name = "상점1", link = "http://shop1.com"),
                PartnerShopEmbeddable(name = "상점2", link = "http://shop2.com"),
            )
        val productEntity =
            ProductEntity(
                productId = 1L,
                userId = 123L,
                title = "테스트 제품",
                description = "이것은 테스트 제품입니다",
                price = 1000L,
                typeCode = 1,
                shootingTime = fixedCreatedAt,
                shootingLocation = "테스트 장소",
                numberOfCostumes = 5,
                partnerShops = partnerShops,
                detailedInfo = "상세 정보",
                warrantyInfo = "1년 보증",
                contactInfo = "test@example.com",
                createdAt = fixedCreatedAt,
                updatedAt = fixedUpdatedAt,
                images = listOf("image1.jpg", "image2.jpg"),
            )

        val productOptionEntity =
            ProductOptionEntity(
                optionId = 1L,
                name = "테스트 옵션",
                additionalPrice = 500L,
                description = "옵션 설명",
                product = productEntity,
                createdAt = fixedCreatedAt,
                updatedAt = fixedUpdatedAt,
            )

        val productOption = productOptionEntity.toDomain()

        productOption.optionId shouldBe productOptionEntity.optionId
        productOption.name shouldBe productOptionEntity.name
        productOption.additionalPrice shouldBe productOptionEntity.additionalPrice
        productOption.description shouldBe productOptionEntity.description
        productOption.productId shouldBe productOptionEntity.product?.productId
        productOption.createdAt shouldBe productOptionEntity.createdAt
        productOption.updatedAt shouldBe productOptionEntity.updatedAt
    }
})
