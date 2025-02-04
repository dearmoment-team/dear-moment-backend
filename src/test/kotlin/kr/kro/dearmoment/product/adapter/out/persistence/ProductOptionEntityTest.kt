package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.ProductOption

class ProductOptionEntityTest : StringSpec({

    "ProductOptionEntity는 도메인 모델에서 올바르게 변환되어야 한다" {
        val partnerShops =
            listOf(
                PartnerShopEmbeddable("상점1", "http://shop1.com"),
                PartnerShopEmbeddable("상점2", "http://shop2.com"),
            )

        // ProductEntity를 만들 때, createdDate / updateDate 미설정 → null
        val productEntity =
            ProductEntity(
                productId = 1L,
                userId = 123L,
                title = "테스트 제품",
                description = "이것은 테스트 제품입니다",
                price = 1000L,
                typeCode = 1,
                shootingTime = null,
                shootingLocation = "테스트 장소",
                numberOfCostumes = 5,
                partnerShops = partnerShops,
                detailedInfo = "상세 정보",
                warrantyInfo = "1년 보증",
                contactInfo = "test@example.com",
                images = listOf("image1.jpg", "image2.jpg"),
            )
        productEntity.createdDate shouldBe null
        productEntity.updateDate shouldBe null

        // 도메인 Option도 시간 미설정
        val productOption =
            ProductOption(
                optionId = 1L,
                productId = 1L,
                name = "테스트 옵션",
                additionalPrice = 500L,
                description = "옵션 설명",
                createdAt = null,
                updatedAt = null,
            )

        val productOptionEntity = ProductOptionEntity.fromDomain(productOption, productEntity)
        productOptionEntity.optionId shouldBe productOption.optionId
        productOptionEntity.name shouldBe productOption.name
        productOptionEntity.additionalPrice shouldBe productOption.additionalPrice
        productOptionEntity.description shouldBe productOption.description
        productOptionEntity.product?.productId shouldBe productOption.productId

        // fromDomain에서 createdDate / updateDate를 null로 유지
        productOptionEntity.createdDate shouldBe null
        productOptionEntity.updateDate shouldBe null
    }

    "ProductOptionEntity는 도메인 모델로 올바르게 변환되어야 한다" {
        val partnerShops =
            listOf(
                PartnerShopEmbeddable("상점1", "http://shop1.com"),
                PartnerShopEmbeddable("상점2", "http://shop2.com"),
            )

        val productEntity =
            ProductEntity(
                productId = 1L,
                userId = 123L,
                title = "테스트 제품",
                description = "이것은 테스트 제품입니다",
                price = 1000L,
                typeCode = 1,
                shootingTime = null,
                shootingLocation = "테스트 장소",
                numberOfCostumes = 5,
                partnerShops = partnerShops,
                detailedInfo = "상세 정보",
                warrantyInfo = "1년 보증",
                contactInfo = "test@example.com",
                images = listOf("image1.jpg", "image2.jpg"),
            )

        // 엔티티에서 time 필드가 null임을 확인
        productEntity.createdDate shouldBe null
        productEntity.updateDate shouldBe null

        val productOptionEntity =
            ProductOptionEntity(
                optionId = 1L,
                name = "테스트 옵션",
                additionalPrice = 500L,
                description = "옵션 설명",
                product = productEntity,
            )
        // 마찬가지로 여기서 createdDate / updateDate = null

        val productOption = productOptionEntity.toDomain()
        productOption.optionId shouldBe productOptionEntity.optionId
        productOption.name shouldBe productOptionEntity.name
        productOption.additionalPrice shouldBe productOptionEntity.additionalPrice
        productOption.description shouldBe productOptionEntity.description
        productOption.productId shouldBe productOptionEntity.product?.productId

        // null 그대로 도메인으로
        productOption.createdAt shouldBe productOptionEntity.createdDate
        productOption.updatedAt shouldBe productOptionEntity.updateDate
    }
})
